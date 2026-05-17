#!/usr/bin/env bash
set -euo pipefail

SESSION_NAME="${TAVALL_CONTROL_PLANE_SESSION_NAME:-tavall-control-plane}"

if ! command -v tmux >/dev/null 2>&1; then
  echo "tmux is required but was not found on PATH." >&2
  exit 1
fi

if tmux has-session -t "$SESSION_NAME" 2>/dev/null; then
  tmux kill-session -t "$SESSION_NAME"
  echo "Stopped tmux session: $SESSION_NAME"
else
  echo "No tmux session named $SESSION_NAME was running."
fi
