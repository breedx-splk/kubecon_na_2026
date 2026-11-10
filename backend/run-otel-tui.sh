#!/usr/bin/env bash
set -euo pipefail

backend_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
otel_tui="$backend_dir/.bin/otel-tui"
if [[ ! -x "$otel_tui" ]]; then
    otel_tui="$(command -v otel-tui || true)"
fi
if [[ -z "$otel_tui" ]]; then
    echo "Run $backend_dir/setup-otel-tui.sh first (or install otel-tui on PATH)." >&2
    exit 1
fi

exec "$otel_tui" --host 0.0.0.0 \
    --grpc "${OTLP_GRPC_PORT:-4317}" --http "${OTLP_HTTP_PORT:-4318}" "$@"
