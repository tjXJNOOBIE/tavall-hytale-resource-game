# Command System

## Purpose
Provide a single inventory of the custom command roots that drive the Minecraft game surface, proxy moderation surface, and control-plane console surface.

## Ownership model
- Minecraft game-server commands render gameplay UI and route player interactions.
- Minecraft proxy commands own moderation and rank entrypoints, while the control plane remains canonical for private state.
- Control-plane console commands own backend inspection, orchestration, and cross-platform state mutation.
- Hytale keeps its own module namespace; shared contracts and backend modules live under the non-Hytale namespace.

## Command inventory
| Root | Owner module | Purpose | Representative subcommands |
| --- | --- | --- | --- |
| `/kingdom`, `/kd` | `minecraft-game-server` + `control-server` | Main in-game kingdom admin, UI, and gameplay surface | `ui`, `data`, `castle`, `buildings`, `interior`, `citizens`, `troops`, `resources`, `account`, `hologram`, `nodes`, `place`, `focus`, `interact`, `scan`, `scene`, `bootstrap`, `tick`, `tutorial`, `entity`, `companion`, `clock`, `schedule`, `aging`, `debug` |
| `/rank` | `minecraft-proxy` + `control-server` | Proxy rank and authority entrypoint backed by canonical control-plane data | `list`, `inspect <player>`, `set <player> <rank>`, `remove <player>` |
| `/ban`, `/warn`, `/unban`, `/unwarn`, `/mute`, `/unmute`, `/kick` | `minecraft-proxy` + `control-server` | Proxy moderation surface with live feedback and backend-backed records | `ban`, `warn`, `unban`, `unwarn`, `mute`, `unmute`, `kick` |
| `cloud` | `cloud-control-plane` + `cloud-agent` | Control-plane orchestration for nodes, workloads, consoles, and kingdom folders | `nodes list`, `nodes register`, `workloads list`, `workloads create`, `workloads reconcile-all`, `servers list`, `consoles list`, `kingdoms list`, `kingdoms ensure` |
| `control` / `panel` | `control-server` | Canonical backend console and panel surface for data inspection and admin work | `help`, `status`, `stop`, `players`, `rank`, `punish`, `kingdoms`, `clock`, `assets`, `audits` |
| `hytale`-backed `/kd` compatibility path | `hytale-frontend` | Legacy in-game compatibility path for Hytale-specific surfaces and shared control-plane input | `ui`, `data`, `castle`, `interior`, `citizens`, `resources`, `account` |

## Notes
- The command surface is intentionally split by runtime responsibility instead of stuffing every action into one adapter.
- `/rank` and moderation commands belong on the proxy surface, but their canonical data still lives in the control plane.
- Minecraft UI commands should open inventory-driven surfaces instead of generic fallback menus.
- Control-plane commands should mutate shared state through backend handlers, not through Minecraft adapters.
- If a command is not listed here, it should be added only after the gameplay or backend flow is actually implemented and tested.
