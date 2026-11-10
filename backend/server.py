#!/usr/bin/env python3
"""Small local demo backend; Python standard library only."""

import argparse
import json
import re
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import urlsplit


ASSETS = Path(__file__).resolve().parents[1] / "app/app/src/main/assets"
ANNOTATION = re.compile(r"^\S+\s+E\d+(?:\.\d+)?\s+(.+)$")


def load_emoji_json():
    """Use the same fully-qualified catalog and exclusions as Android's EmojiCatalog."""
    exclusions = {
        line.strip()
        for line in (ASSETS / "emoji-exclusions.txt").read_text(encoding="utf-8").splitlines()
        if line.strip() and not line.strip().startswith("#")
    }
    entries = {}
    for line in (ASSETS / "emoji-test.txt").read_text(encoding="utf-8").splitlines():
        if not line.strip() or line.startswith("#"):
            continue
        code_points, _, rest = line.partition(";")
        status, _, annotation = rest.partition("#")
        code_points = code_points.strip()
        name = ANNOTATION.fullmatch(annotation.strip())
        if status.strip() != "fully-qualified" or code_points in exclusions or not name:
            continue
        entries.setdefault(code_points, {
            "symbol": "".join(chr(int(point, 16)) for point in code_points.split()),
            "codePoints": code_points,
            "name": name.group(1),
        })
    if not entries:
        raise ValueError("The emoji catalog is empty")
    return json.dumps(list(entries.values()), ensure_ascii=False).encode("utf-8")


class Handler(BaseHTTPRequestHandler):
    emoji_json = b""

    def do_GET(self):
        if urlsplit(self.path).path != "/emoji.json":
            self.send_error(404)
            return
        self.send_response(200)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(self.emoji_json)))
        self.end_headers()
        if self.command != "HEAD":
            self.wfile.write(self.emoji_json)

    do_HEAD = do_GET

    def do_POST(self):
        if urlsplit(self.path).path != "/movie-script":
            self.send_error(404)
            return
        if self.headers.get("Transfer-Encoding"):
            self.send_error(501, "Use Content-Length instead of Transfer-Encoding")
            return
        try:
            remaining = int(self.headers.get("Content-Length", "0"))
            if remaining < 0:
                raise ValueError
        except ValueError:
            self.send_error(400, "Invalid Content-Length")
            return
        # Consume without parsing, logging, or retaining the submitted script.
        while remaining:
            chunk = self.rfile.read(min(remaining, 65536))
            if not chunk:
                self.send_error(400, "Incomplete request body")
                return
            remaining -= len(chunk)
        self.send_response(200)
        self.send_header("Content-Length", "0")
        self.end_headers()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--host", default="0.0.0.0")
    parser.add_argument("--port", type=int, default=8080)
    args = parser.parse_args()
    Handler.emoji_json = load_emoji_json()
    with ThreadingHTTPServer((args.host, args.port), Handler) as server:
        print(f"Backend listening on http://{args.host}:{server.server_port}", flush=True)
        try:
            server.serve_forever()
        except KeyboardInterrupt:
            pass


if __name__ == "__main__":
    main()
