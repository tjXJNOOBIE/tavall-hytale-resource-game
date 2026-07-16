#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if command -v pwsh >/dev/null 2>&1; then
  pwsh -NoProfile -ExecutionPolicy Bypass -File "$script_dir/test-farmstead-right-click-ui.ps1" "$@"
else
  powershell.exe -NoProfile -ExecutionPolicy Bypass -File "$script_dir/test-farmstead-right-click-ui.ps1" "$@"
fi
