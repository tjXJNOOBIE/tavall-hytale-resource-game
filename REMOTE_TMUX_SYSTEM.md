# Remote Tmux Runtime

Use `scripts/start-remote-resource-game-tmux.ps1` to restart the live remote runtime into named tmux sessions without exposing SSH credentials. Use `scripts/start-remote-cloud-control-plane-tmux.ps1` to deploy and start the standalone control-plane jar under `/srv/control-plane`.

| Session | Purpose | Attach command |
|---|---|---|
| `control` | Main resource-game control server on port `18082` | `tmux attach -t control` |
| `minecraft-control` | Minecraft-facing control bridge on port `18081` | `tmux attach -t minecraft-control` |
| `minecraft-proxy` | Velocity proxy on port `25565` | `tmux attach -t minecraft-proxy` |
| `minecraft-ffa` | FFA backend on port `25566` | `tmux attach -t minecraft-ffa` |
| `minecraft-switch` | Switch backend on port `25567` | `tmux attach -t minecraft-switch` |
| `minecraft-kingdom` | Paper `1.21.4` kingdom backend on port `25568` | `tmux attach -t minecraft-kingdom` |
| `cloud-agent` | Optional Tavall Cloud node agent heartbeat/poll/execute loop, started from the consolidated `control-server` jar | `tmux attach -t cloud-agent` |
| `cloud-control-plane` | Standalone plain-Java control plane under `/srv/control-plane` | `tmux attach -t cloud-control-plane` |
| `hytale` | Hytale single-surface runtime using the `minecraft-control` ingress URL for backend command verification | `tmux attach -t hytale` |

PowerShell restart:

```powershell
.\scripts\start-remote-resource-game-tmux.ps1
```

## Repeatable workflow

| Step | Command | Expected result |
|---|---|---|
| Restart sessions | `.\scripts\start-remote-resource-game-tmux.ps1` | Remote tmux sessions are recreated using the configured SSH alias and config |
| Restart control plane | `.\scripts\start-remote-cloud-control-plane-tmux.ps1` | Standalone control-plane jar is deployed to `/srv/control-plane` and started in `cloud-control-plane` |
| Check ports | `ss -ltnp | grep -E ':(18082|18081|25565|25566|25567|25568) '` | Control, Minecraft bridge, proxy, and backends are listening |
| Verify Minecraft proxy | `MINECRAFT_PROXY_COMMANDS='...' node resource-game-minecraft-velocity-flow.mjs ...` | Bot completes Velocity/server command flow |
| Verify Hytale bridge | `tcp://127.0.0.1:18081` via the typed control bridge and live bot harness | HYTALE command envelope succeeds through shared bridge |
| Inspect logs | `tmux attach -t <session>` or `tail -n 120 <runtime>/logs/*.log` | Runtime-specific logs are visible on remote only |

Linux attach commands from inside the remote SSH session:

```bash
tmux attach -t minecraft-proxy
tmux attach -t minecraft-kingdom
tmux attach -t minecraft-control
tmux attach -t control
tmux attach -t cloud-agent
tmux attach -t cloud-control-plane
tmux attach -t hytale
```

Remote port check:

```bash
ss -ltnp | grep -E ':(18082|18081|25565|25566|25567|25568) '
```

Live ingress checks:

```bash
cd /srv/headless
MINECRAFT_PROXY_COMMANDS='/server kingdom|/tavallserver snapshot|/tavallserver visual title Server visual path online|/tavallserver interact diagnostic server event|/kd clock state kingdom-1|/kingdom citizens summary kingdom-1' node resource-game-minecraft-velocity-flow.mjs 127.0.0.1 25565 ResourceProxyBot 1.21.4 /tmp/resource-game-live-verify
npx tsx scripts/run-remote-minecraft-velocity-control-plane-flow.ts --command-list \"/kd clock state kingdom-1\" --server-command-list \"!/rank inspect miner-1\"
```

Latest verified runtime:

| Runtime check | Result |
|---|---|
| `control` | Port `18082` open |
| `minecraft-control` | Port `18081` open |
| `minecraft-proxy` | Port `25565` open |
| `minecraft-ffa` | Port `25566` open |
| `minecraft-switch` | Port `25567` open |
| `minecraft-kingdom` | Port `25568` open |
| `cloud-agent` | Optional session exists when the remote control runtime can launch `org.tavall.control.cloud.CloudAgentApplication` from the consolidated `control-server` jar |
| Minecraft command flow | `/server kingdom`, `/tavallserver snapshot`, `/tavallserver visual title ...`, `/tavallserver interact ...`, `/kd clock state kingdom-1`, and `/kingdom citizens summary kingdom-1` completed |
| Hytale ingress flow | `HYTALE` `/kd clock state kingdom-1` envelope completed through `minecraft-control` |
| Updated Hytale plugin | Rebuilt `tavall-hytale-resource-game.jar` deployed to `/srv/hytale/HytaleDevServer/Server/mods` and verified after tmux restart |

## Safety rules

| Area | Rule |
|---|---|
| SSH | Use existing SSH alias/config parameters; do not print, copy, generate, or rewrite private keys. |
| Secrets | Do not echo tokens, session credentials, identity tokens, or full environment dumps. |
| Remote scripts | Keep command ingress URLs, server IDs, and tmux session names explicit and repeatable. |
| Logs | Store local bot artifacts under `bot-logs`; inspect remote runtime logs in place when possible. |
| Cleanup | Kill/restart named tmux sessions and known ports only, not broad unrelated processes. |
