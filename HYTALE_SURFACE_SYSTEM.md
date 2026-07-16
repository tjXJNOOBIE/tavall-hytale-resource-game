# Hytale Surface System

Hytale is currently a single frontend runtime surface. It renders player-facing UI/world interactions and submits canonical commands/events to the Java middleware instead of owning long-lived gameplay state.

## Surface matrix

| Surface area | Hytale responsibility | Canonical owner | Notes |
|---|---|---|---|
| `/kd` commands | Parse Hytale command input and forward/execute through shared command handlers | Command system/control plane | Keep command aliases compatible while migration continues. |
| Custom UI pages | Render page models, dispatch button/action ids | UI system and backend page model handlers | UI action ids must map back to backend handlers. |
| World interaction | Focus, prompt lanes, right-click/action input | Interaction bridge and backend events | Frontend input is not game logic mutation by itself. |
| Castle visuals | Markers, labels, castle scene refresh | Castle services and projections | Visual refresh follows backend mutation success. |
| Interior runtime | Teleport/instance presentation in the same process | Interior services | Interiors are separate worlds, not separate servers in the current runtime. |
| Bot harness hooks | QUIC/headless snapshots and commands | Test system and command system | Bots use command/focus helpers until native click packets are available. |
| Remote ingress check | HYTALE `FrontendCommandEnvelope` POST | Shared control ingress | Used by tmux live verification. |

## Command ingress

| Input | Envelope/platform | Expected backend path |
|---|---|---|
| `/kd clock state kingdom-1` | `platform=HYTALE`, `surface=COMMAND` | Frontend command ingress -> control command dispatch -> clock handler |
| `/kd citizens summary kingdom-1` | `platform=HYTALE`, `surface=COMMAND` | Frontend command ingress -> citizen command handler |
| UI button action | `platform=HYTALE`, `surface=UI_ACTION` | UI action service -> backend handler/event |
| World interaction | `platform=HYTALE`, `surface=INTERACTION` | Interaction bridge -> typed backend game event |

## Remote verification

| Check | Command |
|---|---|
| Shared control bridge | `tcp://127.0.0.1:18081` via `FrontendTcpControlCommandClient` and the typed bridge path |
| Tmux runtime | `tmux attach -t hytale` |
| Remote restart | `.\scripts\start-remote-resource-game-tmux.ps1` |

The Hytale surface may use local in-process handlers while active migration is underway, but new canonical logic belongs behind shared Java command/event services so Minecraft, Roblox, Discord, and future apps can consume the same behavior.
