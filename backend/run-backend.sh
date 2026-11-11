#!/usr/bin/env bash
set -euo pipefail

backend_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
python="$backend_dir/.venv/bin/python"
if [[ ! -x "$python" ]]; then
    echo "Run $backend_dir/setup-backend.sh first to install the tracing dependencies." >&2
    exit 1
fi
exec "$python" -u "$backend_dir/server.py" --host 0.0.0.0 --port "${BACKEND_PORT:-8080}" "$@"
