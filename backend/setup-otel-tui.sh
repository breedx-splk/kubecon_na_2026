#!/usr/bin/env bash
set -euo pipefail

# Keep the demo reproducible and install only inside this directory.
version=v0.7.5
backend_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
platform="$(uname -s)"
architecture="$(uname -m)"
case "$platform/$architecture" in
    Darwin/arm64|Darwin/x86_64|Linux/arm64|Linux/x86_64) ;;
    Linux/aarch64) architecture=arm64 ;;
    *) echo "Unsupported platform: $platform/$architecture" >&2; exit 1 ;;
esac
command -v gh >/dev/null || { echo "Install the GitHub CLI (gh) first." >&2; exit 1; }

archive="otel-tui_${platform}_${architecture}.tar.gz"
checksums="otel-tui_${version#v}_checksums.txt"
download_dir="$(mktemp -d)"
trap 'rm -rf -- "$download_dir"' EXIT
gh release download "$version" --repo ymtdzzz/otel-tui --dir "$download_dir" \
    --pattern "$archive" --pattern "$checksums"
awk -v archive="$archive" '$2 == archive {print}' "$download_dir/$checksums" > "$download_dir/checksum.txt"
if [[ ! -s "$download_dir/checksum.txt" ]]; then
    echo "No checksum found for $archive" >&2
    exit 1
fi
(
    cd -- "$download_dir"
    if command -v sha256sum >/dev/null; then
        sha256sum -c checksum.txt
    else
        shasum -a 256 -c checksum.txt
    fi
)
tar -xzf "$download_dir/$archive" -C "$download_dir" otel-tui
mkdir -p "$backend_dir/.bin"
install -m 755 "$download_dir/otel-tui" "$backend_dir/.bin/otel-tui"
"$backend_dir/.bin/otel-tui" --version
