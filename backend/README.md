# Demo backend

Run directly on the host with Python 3.7+ and bash. No Python packages are required.

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

`POST /movie-script` reads and discards the body and returns **200** with an empty
response. It accepts any content type and does not parse or store the submitted
script. Send a `Content-Length` (normal for a string/JSON POST); chunked transfer
encoding is not supported. Unknown paths return 404. Access logs go to the terminal.

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
