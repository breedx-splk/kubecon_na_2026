#!/usr/bin/env bash
set -euo pipefail

backend_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
exec python3 -u "$backend_dir/server.py" --host 0.0.0.0 --port "${BACKEND_PORT:-8080}" "$@"
