# Minecraft Surface System

Minecraft has two plugin surfaces: Velocity for global/proxy work and Bukkit/Paper for server-side data and visuals.

## Surface responsibilities

| Surface | Artifact | Responsibilities |
|---|---|---|
| Velocity proxy | `tavall-resource-game-minecraft-frontend.jar` | `/kd`, `/kingdom`, permissions, routing, instance switching, global responses |
| Bukkit/Paper server | `tavall-resource-game-minecraft-server-frontend.jar` | Player/world snapshots, server-only interactions, scoreboard/actionbar/bossbar/chat/inventory visual hooks |
| Hytale | Hytale frontend plugin | Single-surface frontend, no proxy split |
| Custom runtime | Contract adapter | Snapshot logging/no-op visuals until runtime has full world support |

## Remote deployment paths

| Target | Path |
|---|---|
| Velocity proxy plugin | `/srv/proxy/plugins` |
| FFA server plugin | `/srv/ffa/plugins` |
| Switch server plugin | `/srv/ffa-switch/plugins` |
| Kingdom server plugin | `/srv/mc-kingdom-servers/mc-kingdom-server-1/plugins` |

## Verification commands

| Command | Expected surface |
|---|---|
| `/server kingdom` | Velocity switches to registered `kingdom` backend |
| `/tavallserver snapshot` | Bukkit/Paper server plugin posts snapshot to control ingress |
| `/kd clock state kingdom-1` | Velocity command reaches control plane |
| `/kingdom citizens summary kingdom-1` | Velocity command response still works |

The kingdom backend is the Paper 1.21.4 target. Legacy FFA surfaces may stay on their current Spigot setup while the kingdom server advances independently.
