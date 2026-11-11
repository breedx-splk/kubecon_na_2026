"""OTLP/HTTP tracing for the standard-library HTTP server."""

import os
from contextlib import ExitStack
from http.server import BaseHTTPRequestHandler
from urllib.parse import urlsplit

from opentelemetry import propagate, trace
from opentelemetry.exporter.otlp.proto.http.trace_exporter import OTLPSpanExporter
from opentelemetry.sdk.resources import Resource
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import BatchSpanProcessor
from opentelemetry.trace import SpanKind, StatusCode


def configure_tracing():
    """Called once at startup; the exporter honors standard OTEL_* overrides."""
    os.environ.setdefault("OTEL_SERVICE_NAME", "lazy-bonez-backend")
    os.environ.setdefault("OTEL_RESOURCE_ATTRIBUTES", "deployment.environment.name=development")
    os.environ.setdefault("OTEL_EXPORTER_OTLP_ENDPOINT", "http://localhost:4318")
    os.environ.setdefault("OTEL_BSP_SCHEDULE_DELAY", "1000")
    provider = TracerProvider(resource=Resource.create())
    provider.add_span_processor(BatchSpanProcessor(OTLPSpanExporter()))
    trace.set_tracer_provider(provider)
    return provider, provider.get_tracer("lazy-bonez.backend.http")


class TracedHTTPRequestHandler(BaseHTTPRequestHandler):
    tracer = None
    routes = frozenset()

    def handle_one_request(self):
        # Start after headers are parsed, end after the response body is sent.
        # Each request gets its own context, even on a reused connection.
        with ExitStack() as stack:
            self._span_stack = stack
            self._request_span = None
            try:
                super().handle_one_request()
            except Exception as error:
                if self._request_span is not None:
                    self._request_span.set_attribute("error.type", type(error).__qualname__)
                # The span context records the exception and sets ERROR on exit.
                raise

    def parse_request(self):
        if not super().parse_request():
            return False
        method = self.command
        if method not in {"CONNECT", "DELETE", "GET", "HEAD", "OPTIONS", "PATCH", "POST", "PUT", "TRACE"}:
            method = "_OTHER"
        attributes = {"http.request.method": method, "url.scheme": "http"}
        if method != self.command:
            attributes["http.request.method_original"] = self.command
        path = urlsplit(self.path).path
        route = path if path in self.routes else None
        if route:
            attributes["http.route"] = route
        self._request_span = self._span_stack.enter_context(
            self.tracer.start_as_current_span(
                f"{method} {route}" if route else method,
                context=propagate.extract(self.headers),
                kind=SpanKind.SERVER,
                attributes=attributes,
            )
        )
        return True

    def send_response(self, code, message=None):
        if self._request_span is not None:
            self._request_span.set_attribute("http.response.status_code", code)
            if code >= 500:
                self._request_span.set_status(StatusCode.ERROR)
                self._request_span.set_attribute("error.type", str(code))
        super().send_response(code, message)
