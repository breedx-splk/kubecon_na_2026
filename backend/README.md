# Demo backend

Run directly on the host with Python 3.10+ and bash. Install the backend's
OpenTelemetry dependencies once in a local virtual environment:

```sh
./backend/setup-backend.sh
```

Install the pinned [otel-tui](https://github.com/ymtdzzz/otel-tui) release once:

```sh
./backend/setup-otel-tui.sh
```

The setup script uses the GitHub CLI (`gh`) to download v0.7.5, verifies its release
checksum, and puts the executable in the ignored `backend/.bin/` directory. An
existing `otel-tui` on PATH also works if you skip setup.

Run these in two terminals, from the repository root:

```sh
./backend/run-backend.sh
```

```sh
./backend/run-otel-tui.sh
```

Both bind to all IPv4 interfaces (`0.0.0.0`). The TUI stays in the foreground.
Stop either service with Ctrl-C. Scripts also work from any
working directory when invoked by their full path.

| Service | Default port |
| --- | --- |
| HTTP backend | 8080 |
| OTLP/gRPC (traces, metrics, logs) | 4317 |
| OTLP/HTTP (traces, metrics, logs) | 4318 |

Override ports with `BACKEND_PORT`, `OTLP_GRPC_PORT`, and `OTLP_HTTP_PORT`.
Extra command-line arguments are passed through to Python or otel-tui.

## Tracing

The backend exports HTTP server spans as OTLP/HTTP protobuf to
`http://localhost:4318/v1/traces`, under service name `lazy-bonez-backend`.
Start otel-tui before sending requests and restart the backend after code changes.
Spans export in the background about once a second; Ctrl-C flushes pending spans.

Incoming W3C `traceparent`/`tracestate` headers are extracted so backend requests
join the Android app's traces. Spans cover request handling through response
writing, include method, matched route and response status, and mark 5xx responses
and unhandled exceptions as errors. Unknown URLs use the method alone as the span
name. Request bodies are not added to spans. Terminal request logging stays enabled.

Standard OTel environment variables override the defaults (also listed in
`.env.example`; the backend does not automatically load `.env` files):

| Variable | Default |
| --- | --- |
| `OTEL_SERVICE_NAME` | `lazy-bonez-backend` |
| `OTEL_RESOURCE_ATTRIBUTES` | `deployment.environment.name=development` |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | `http://localhost:4318` |
| `OTEL_BSP_SCHEDULE_DELAY` | `1000` milliseconds |

`OTEL_EXPORTER_OTLP_TRACES_ENDPOINT` can override the full traces URL, including
`/v1/traces`. Export uses HTTP/protobuf. For example:

```sh
OTEL_SERVICE_NAME=demo-backend ./backend/run-backend.sh
```

## HTTP endpoints

`GET /emoji.json` returns a UTF-8 JSON array for use by each of the three wheels:

```json
[
  {"symbol": "😀", "codePoints": "1F600", "name": "grinning face"}
]
```

The complete response uses the app's existing `emoji-test.txt` and
`emoji-exclusions.txt` in `app/app/src/main/assets/`, with the same fully-qualified
filter and deduplication as `EmojiCatalog.kt`. Restart the backend after editing
the catalog or exclusions. All wheels share this pool and can shuffle it separately.

`POST /movie-script`, `POST /results/emoji`, and `POST /results/plot` read and discard
the body and return **200** with an empty
response. These endpoints accept any content type and do not parse or store the submitted
results. Send a `Content-Length` (normal for a string/JSON POST); chunked transfer
encoding is not supported. Unknown paths return 404. Every request logs its timestamp,
client IP, method, URL, and response status to stdout immediately, including errors.

After successful plot generation, the app submits JSON to both result endpoints:

- `/results/emoji`: `{"emojis": [{"symbol": "😀", "codePoints": "1F600", "name": "grinning face"}, ...]}` with all three selected entries in wheel order.
- `/results/plot`: `{"plot": "The generated movie synopsis."}`.

Both calls use the app's instrumented OkHttp client. Their HTTP spans are children
of `slot_machine.spin`, which remains open until both POST attempts finish.
`slot_machine.stop` ends after plot generation, before result submission.
Restart the backend after updating its routes.

```sh
curl http://localhost:8080/emoji.json
curl -i http://localhost:8080/movie-script \
  -H 'Content-Type: application/json' \
  -d '{"script":"Something spooky happens."}'
```

## Connecting the app

Use `10.0.2.2` as the host from the standard Android emulator, or the computer's
LAN IP from a phone on the same network. For example, the backend base URL is
`http://10.0.2.2:8080` and the OTLP/HTTP base URL is `http://10.0.2.2:4318`
(`/v1/traces`, `/v1/metrics`, and `/v1/logs` are the signal paths).

For a USB-connected phone, an alternative is:

```sh
adb reverse tcp:8080 tcp:8080
adb reverse tcp:4317 tcp:4317
adb reverse tcp:4318 tcp:4318
```

Then use `127.0.0.1` on the phone. The Android app will need Internet permission
and cleartext HTTP enabled for these local URLs when the client is wired up.
These services are for the local demo and have no authentication or TLS.
