# Distributed Runtime System

The distributed layer lets local dev, remote servers, Hytale processes, Minecraft surfaces, backend services, Redis/Postgres, bots, and admin tools coordinate without frontend events mutating game state.

## Node types

| Type | Purpose |
|---|---|
| LOCAL_DEV | Local tools and smoke tests |
| REMOTE_DEV | Remote dev server |
| HYTALE_GAME_SERVER | Hytale gameplay runtime |
| INTERIOR_SERVER | Interior/world instance runtime |
| CONTROL_NODE | Control-plane supporting node |
| BOT_TEST_NODE | Bot harness worker |

## Node statuses

| Status | Meaning |
|---|---|
| STARTING | Process exists but not ready |
| ONLINE | Heartbeating and schedulable |
| DEGRADED | Running with health issues |
| OFFLINE | Missing heartbeat/unreachable |
| DRAINING | Avoid new work and move/stop workloads |
| FAILED | Known failed state requiring intervention |

## Capabilities

| Capability | Meaning |
|---|---|
| CAN_RUN_GAME | Can host game workloads |
| CAN_RUN_INTERIORS | Can host interior instances |
| CAN_RUN_BOTS | Can run bot tests |
| CAN_DISPATCH_EVENTS | Can forward backend game events |
| CAN_RUN_ADMIN_UI | Can host admin UI |
| CAN_WRITE_DATABASE | Can write durable state |
| CAN_USE_REDIS | Can publish/cache via Redis |

## First-pass runtime features

| Feature | Status |
|---|---|
| Node registry | Domain model and cloud node registry slice |
| Heartbeats | Node heartbeat handler and alert hook |
| Remote test bridge | Existing SSH-script flow, no secret printing |
| Redis pub/sub event channel | TODO adapter after repo cache conventions are wired |
| Admin debug commands | Routed through control command pipeline |
| Bot testing | Remote Minecraft proxy flow exercises `/server`, server snapshot, `/kd`, and `/kingdom` |
