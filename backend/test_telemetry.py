"""Run with: backend/.venv/bin/python -m unittest discover -s backend"""

import http.client
import json
import socket
import threading
import time
import unittest
from concurrent.futures import ThreadPoolExecutor
from http.server import ThreadingHTTPServer

from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import SimpleSpanProcessor
from opentelemetry.sdk.trace.export.in_memory_span_exporter import InMemorySpanExporter
from opentelemetry.trace import SpanKind, StatusCode

from server import Handler, load_emoji_json


class TracingTests(unittest.TestCase):
    def setUp(self):
        self.exporter = InMemorySpanExporter()
        self.provider = TracerProvider()
        self.provider.add_span_processor(SimpleSpanProcessor(self.exporter))

        class TestHandler(Handler):
            def log_message(self, format, *args):
                pass

            def do_GET(self):
                if self.path == "/test-error":
                    raise RuntimeError("test failure")
                super().do_GET()

        TestHandler.tracer = self.provider.get_tracer("test")
        TestHandler.emoji_json = load_emoji_json()
        self.server = ThreadingHTTPServer(("127.0.0.1", 0), TestHandler)
        # Expected exceptions are inspected through spans instead of terminal output.
        self.server.handle_error = lambda request, address: None
        self.thread = threading.Thread(target=self.server.serve_forever)
        self.thread.start()
        self.addCleanup(self.stop_server)

    def stop_server(self):
        self.server.shutdown()
        self.server.server_close()
        self.thread.join()
        self.provider.shutdown()

    def request(self, method, path, body=None, headers=None):
        connection = http.client.HTTPConnection(*self.server.server_address, timeout=3)
        try:
            connection.request(method, path, body=body, headers=headers or {})
            response = connection.getresponse()
            return response.status, response.read()
        finally:
            connection.close()

    def spans(self, count=1):
        deadline = time.monotonic() + 2
        while len(self.exporter.get_finished_spans()) < count and time.monotonic() < deadline:
            time.sleep(0.01)
        spans = self.exporter.get_finished_spans()
        self.assertEqual(len(spans), count)
        return spans

    def test_get_and_head(self):
        status, body = self.request("GET", "/emoji.json?wheel=2")
        self.assertEqual(status, 200)
        self.assertGreater(len(json.loads(body)), 3000)
        self.assertEqual(self.request("HEAD", "/emoji.json"), (200, b""))
        for span in self.spans(2):
            self.assertEqual(span.kind, SpanKind.SERVER)
            self.assertEqual(span.attributes["http.route"], "/emoji.json")
            self.assertEqual(span.attributes["http.response.status_code"], 200)
            self.assertEqual(span.status.status_code, StatusCode.UNSET)

    def test_posts_preserve_remote_parent_and_tracestate(self):
        headers = {
            "Traceparent": "00-0123456789abcdef0123456789abcdef-0123456789abcdef-01",
            "Tracestate": "demo=value",
        }
        paths = ["/movie-script", "/results/emoji", "/results/plot"]
        for path in paths:
            self.assertEqual(self.request("POST", path, b'{"plot":"boo"}', headers), (200, b""))
        for span in self.spans(3):
            self.assertEqual(span.context.trace_id, int("0123456789abcdef0123456789abcdef", 16))
            self.assertEqual(span.parent.span_id, int("0123456789abcdef", 16))
            self.assertTrue(span.parent.is_remote)
            self.assertEqual(span.context.trace_state.get("demo"), "value")
            self.assertIn(span.attributes["http.route"], paths)
            self.assertTrue(span.name.startswith("POST /"))

    def test_unknown_paths_have_bounded_names_and_4xx_is_not_error(self):
        self.assertEqual(self.request("GET", "/missing/123?secret=value")[0], 404)
        span, = self.spans()
        self.assertEqual(span.name, "GET")
        self.assertNotIn("http.route", span.attributes)
        self.assertEqual(span.status.status_code, StatusCode.UNSET)
        self.assertEqual(span.attributes["http.response.status_code"], 404)

    def test_5xx_is_error(self):
        self.assertEqual(self.request("DELETE", "/emoji.json")[0], 501)
        span, = self.spans()
        self.assertEqual(span.status.status_code, StatusCode.ERROR)
        self.assertEqual(span.attributes["error.type"], "501")

    def test_exception_is_recorded(self):
        with self.assertRaises(http.client.RemoteDisconnected):
            self.request("GET", "/test-error")
        span, = self.spans()
        self.assertEqual(span.status.status_code, StatusCode.ERROR)
        self.assertEqual(span.attributes["error.type"], "RuntimeError")
        self.assertEqual([event.name for event in span.events], ["exception"])

    def test_concurrent_requests_have_separate_contexts(self):
        with ThreadPoolExecutor(max_workers=4) as pool:
            results = list(pool.map(lambda _: self.request("POST", "/movie-script", b"{}"), range(8)))
        self.assertEqual(results, [(200, b"")] * 8)
        spans = self.spans(8)
        self.assertEqual(len({span.context.trace_id for span in spans}), 8)
        self.assertTrue(all(span.parent is None for span in spans))

    def test_empty_connection_does_not_create_span(self):
        with socket.create_connection(self.server.server_address, timeout=3):
            pass
        self.server.shutdown()
        self.thread.join()
        self.assertEqual(self.exporter.get_finished_spans(), ())


if __name__ == "__main__":
    unittest.main()
