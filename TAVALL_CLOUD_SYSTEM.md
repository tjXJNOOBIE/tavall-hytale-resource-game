# Tavall Cloud / Novus Cloud

Tavall Cloud is a plain Java owned-server control plane. Spring can provide an optional panel later, but Spring controllers must not own infrastructure state or bypass command handlers.

## Runtime layers

| Layer | Responsibility | Source of truth |
|---|---|---|
| Memory | Hot node/workload summaries, scheduler candidates, recent alerts | Rebuildable cache |
| Redis | Heartbeats, command queue summaries, dirty tags, locks, recent metric windows | Fast shared cache |
| Postgres | Node registry, workload desired state, commands, audit, identities, join tokens | Durable source |
| Object storage | Backups, logs, snapshots, artifacts | Optional durable artifacts |

## Core components

| Component | Owns |
|---|---|
| CloudControlPlane | Runtime composition and command/query entrypoints |
| NodeRegistrationRequestHandler | Join-token node import |
| NodeHeartbeatHandler | Hot health updates |
| NodeSchedulerHandler | First-pass deterministic placement |
| WorkloadRequestHandler | Desired workload creation |
| WorkloadReconciliationHandler | Desired vs actual command generation |
| CloudCommandCreationHandler | Typed command envelopes |
| CloudCommandResultHandler | Result recording and redaction |
| PortAllocationHandler | Port reservations |
| BackupJobCreationHandler | Backup jobs and RUN_BACKUP commands |
| AlertEvaluationHandler | Health and failure alerts |

## Workload types

| Type | Required examples |
|---|---|
| MINECRAFT_SERVER | Java runtime, persistent volume, TCP port, optional Velocity route |
| HYTALE_SERVER | Hytale runtime, assets/config/world storage |
| INTERIOR_SERVER | Hytale/interior capability, linked instance metadata |
| BOT_TEST_WORKER | Bot runtime, temporary logs/artifacts |
| REDIS | Persistent data volume, health check |
| POSTGRES | Database volume, backup plan |
| QDRANT | Persistent volume, health check |
| VELOCITY_PROXY | Public port, routing config |
| SPRING_PANEL | Optional HTTP route, no canonical state ownership |

## Command bus

| Command | Normal executor |
|---|---|
| INSTALL_WORKLOAD | Node agent runtime adapter |
| START_WORKLOAD | Node agent |
| STOP_WORKLOAD | Node agent |
| RESTART_WORKLOAD | Node agent |
| DELETE_WORKLOAD | Node agent, high-risk gated |
| OPEN_PORT / CLOSE_PORT | Node agent networking adapter |
| APPLY_FIREWALL_RULES | Node agent networking adapter |
| APPLY_PROXY_CONFIG | Proxy node agent |
| RUN_BACKUP / RESTORE_BACKUP | Backup-capable node agent |

Current first pass is in-memory and testable. Production TODOs: Postgres/Redis adapters, command signing, DNS provider adapter, object storage adapter, credential rotation, and optional Spring panel pages styled after the Webstore dashboard.
