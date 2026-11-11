#!/usr/bin/env bash
set -euo pipefail

backend_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
python3 -m venv "$backend_dir/.venv"
"$backend_dir/.venv/bin/python" -m pip install -r "$backend_dir/requirements.txt"
