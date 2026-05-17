# Tavall Cloud / Novus Cloud

Tavall Cloud is the owned-server/private-cloud control plane for raw machines. The core control plane is plain Java. Spring can expose an optional web panel, but Spring controllers must stay thin and call Java command/query handlers directly.

## Package Ownership

On `minecraft-main`, cloud/runtime code lives inside the single `control-server` backend module and is separated by package concern instead of sibling Maven modules.

| Package area | Owns | Must not own |
|---|---|---|
| `com.tavall.resourcegame.middleware.cloud` | Cloud domain models, command types, repository ports, in-memory repository, reconciliation, alerts, agent/runtime entrypoints | Minecraft adapter state |
| `com.tavall.resourcegame.controlserver.cli` | Plain Java control-plane console and command entrypoint | Canonical cloud state |
| `com.tavall.resourcegame.controlserver.web` | Optional Spring/web adapter into Java handlers | Canonical cloud state |

## Runtime Layers

| Layer | Responsibility | Source of truth |
|---|---|---|
| Memory | Hot node/workload summaries, scheduler candidates, recent alerts | Rebuildable cache |
| Redis | Heartbeats, command summaries, dirty tags, locks, recent metric windows | Fast shared cache |
| Postgres | Node registry, workload desired state, commands, audit, identities, join tokens | Durable source |
| Object storage | Backups, logs, snapshots, artifacts | Optional durable artifacts |

## Control Plane Filesystem Layout

The standalone control-plane runtime is deployed under `/srv/control-plane` on remote hosts. The remote launcher writes the jar, launch script, logs, caches, and kingdom folders into that directory and sets `TAVALL_CLOUD_CONTROL_PLANE_ROOT` before starting the console.

| Path | Purpose |
|---|---|
| `/srv/control-plane/cloud-control-plane.jar` | Deployable control-plane jar (sourced from the consolidated `control-server` build) |
| `/srv/control-plane/start.sh` | Remote launch script used by tmux |
| `/srv/control-plane/logs` | Control-plane logs |
| `/srv/control-plane/workloads` | Workload metadata and runtime state |
| `/srv/control-plane/consoles` | Console/session references |
| `/srv/control-plane/cache` | Local control-plane cache scratch space |
| `/srv/control-plane/kingdoms/kingdom-1` | Default kingdom data folder |
| `/srv/control-plane/kingdoms/<kingdomId>` | Additional kingdom data folders |

`CloudControlPlaneFilesystemLayout` creates and normalizes this layout so the control plane can launch workloads, store kingdom data, and expose tmux attach targets without using HTTP for private state.

## Control Plane Components

| Component | Current behavior |
|---|---|
| `NodeRegistrationRequestHandler` | Validates hashed one-time join tokens and creates node identity/runtime records |
| `NodeHeartbeatHandler` | Updates node heartbeat and hot status |
| `NodeHealthEvaluationHandler` | Marks nodes online/degraded/offline and creates alerts |
| `NodeSchedulerHandler` | Deterministic scoring by health, region, capability, resources, and architecture |
| `WorkloadRequestHandler` | Creates desired workload records after validation |
| `WorkloadReconciliationHandler` | Compares desired/actual state and creates typed commands without duplicate pending commands |
| `CloudCommandCreationHandler` | Creates allowlisted typed command envelopes |
| `CloudCommandAuthorizationHandler` | Wraps cloud commands into C1-C6 `ControlCommandRequest` checks |
| `CloudCommandResultHandler` | Records command results and redacts sensitive output |
| `PortAllocationHandler` | Reserves/releases ports and rejects collisions |
| `BackupJobCreationHandler` | Creates backup jobs and `RUN_BACKUP` commands |
| `RestoreJobCreationHandler` | Creates restore jobs behind permission hooks |
| `AlertEvaluationHandler` | Emits alerts for heartbeat, resource, backup, and command failures |
| `CloudAgentIngressController` | Thin Spring adapter for agent heartbeat, command polling, and result reporting |

## Agent HTTP Boundary

| Endpoint | Method | Java handler path | Behavior |
|---|---|---|---|
| `/api/cloud/agent/{nodeId}/heartbeat` | `POST` | `NodeHeartbeatHandler` | Accepts `CloudAgentHeartbeatPayload`, rejects node-id mismatches, updates registered node status |
| `/api/cloud/agent/{nodeId}/commands` | `GET` | `AgentCommandPollHandler` | Returns pending typed commands and marks them `SENT` to avoid repeated delivery |
| `/api/cloud/agent/{nodeId}/results` | `POST` | `AgentCommandResultReportHandler` -> `CloudCommandResultHandler` | Records success/failure, redacts command output summaries, rejects node-id mismatches |

`CloudAgentHeartbeatPayload` now lives in the consolidated backend package tree so the agent and optional Spring adapter share the same transport contract without reviving separate cloud Maven modules on `minecraft-main`.

## Workload Matrix

| Workload type | Required capability | Storage | Network | Notes |
|---|---|---|---|---|
| `MINECRAFT_SERVER` | `CAN_RUN_MINECRAFT` | Persistent world/config/log volume | TCP game port | Optional Velocity route metadata |
| `HYTALE_SERVER` | `CAN_RUN_HYTALE` | Persistent world/config/assets volume | Game bridge ports | Single-surface frontend |
| `INTERIOR_SERVER` | `CAN_RUN_HYTALE` | Instance/world volume | Internal route | Linked to kingdom/interior metadata |
| `BOT_TEST_WORKER` | `CAN_RUN_BOTS` | Temporary artifacts/logs | Optional outbound | Safe for short-lived test workloads |
| `REDIS` | `CAN_RUN_REDIS` | Database volume | Redis port | Cache layer, not durable-only truth |
| `POSTGRES` | `CAN_RUN_POSTGRES` | Database volume | Postgres port | Durable source of truth |
| `QDRANT` | `CAN_RUN_QDRANT` | Persistent volume | Qdrant port | Vector/index workload |
| `VELOCITY_PROXY` | `CAN_RUN_PROXY` | Config/log volume | Public game port | Routes Minecraft backends |
| `SPRING_PANEL` | `CAN_RUN_WEB_PANEL` | Config/log volume | HTTP route | Optional admin UI |
| `BACKUP_WORKER` | `CAN_RUN_BACKUPS` | Backup/object target | Internal/outbound | Runs backup/verification jobs |

## Command Bus

| Command | Normal executor | Risk notes |
|---|---|---|
| `INSTALL_WORKLOAD` | Node agent runtime adapter | Validate payload schema |
| `START_WORKLOAD` | Node agent runtime adapter | Runtime must be declared |
| `STOP_WORKLOAD` | Node agent runtime adapter | Scoped workload only |
| `RESTART_WORKLOAD` | Node agent runtime adapter | Scoped workload only |
| `DELETE_WORKLOAD` | Node agent runtime adapter | High-risk, permission gated |
| `OPEN_PORT` / `CLOSE_PORT` | Node networking adapter | Desired state first |
| `APPLY_FIREWALL_RULES` | Node networking adapter | Allowlisted payload only |
| `APPLY_PROXY_CONFIG` | Proxy node adapter | Rendered config, not arbitrary shell |
| `RUN_HEALTH_CHECK` | Node agent | Safe diagnostic |
| `COLLECT_LOGS` | Node agent | Redact sensitive output |
| `RUN_BACKUP` | Backup-capable node agent | Store checksum/result |
| `RESTORE_BACKUP` | Backup-capable node agent | High-risk, approval hook |
| `UPDATE_AGENT` | Node agent | Production hardening TODO |
| `DRAIN_NODE` | Node agent/control plane | Permission gated |
| `CANCEL_COMMAND` | Control plane/agent | Idempotent |

## CLI Command Matrix

| Command family | Examples | Behavior |
|---|---|---|
| Nodes | `cloud nodes create-token`, `cloud nodes list`, `cloud nodes inspect <nodeId>` | Import-token creation and node inventory queries |
| Workloads | `cloud workloads create <type> <name> [region]`, `cloud workloads list`, `cloud workloads start|stop|restart|delete <workloadId>` | Desired-state mutation followed by reconciliation |
| Servers | `cloud servers list`, `cloud servers inspect <workloadId>`, `cloud servers attach <target>` | Friendly inventory and tmux attach helpers for currently running game servers |
| Consoles | `cloud consoles list`, `cloud consoles attach <target>` | Lists the control-plane console and each workload console with attach commands |
| Kingdoms | `cloud kingdoms list`, `cloud kingdoms ensure <kingdomId>` | Lists or creates kingdom data folders under the control-plane root |
| Reconciliation | `cloud workloads reconcile <workloadId>`, `cloud workloads reconcile-all` | Compares desired/actual state and emits missing typed commands |
| Scheduler | `cloud scheduler plan <type> <name> [region]`, `cloud scheduler candidates <type> <name> [region]` | Dry-run placement and candidate/rejection explanation |
| Networking | `cloud ports allocate <workloadId> <publicPort> [internalPort] [TCP|UDP]` | Reserves public/internal game or service ports |
| Backups | `cloud backups list`, `cloud backups run <workloadId> <sourcePath> <destination>` | Creates backup plan/job records and a `RUN_BACKUP` command |
| Commands | `cloud commands list`, `cloud commands inspect|retry|cancel <commandId>` | Command history, requeue, and cancellation request path |
| Observability | `cloud alerts list`, `cloud alerts ack <alertId>`, `cloud metrics node|workload <id>` | Alert and metric inspection from repository state |

The CLI handler uses built-in workload profiles for first-pass creation. Durable workload template storage is a later extension, but command execution already routes through scheduler, reconciliation, authorization, and typed command creation.

## Agent Security

| Boundary | Current status |
|---|---|
| Typed command allowlist | Implemented in `AgentCommandExecutionHandler` |
| Expiry rejection | Implemented |
| Raw shell rejection | Runtime adapters reject `shellCommand` and `commandLine` payloads |
| Command signing | Optional HMAC-SHA256 check via `CloudAgentSecurityConfig` and `CloudCommandSignatureHandler` |
| Secret printing | Command results are summarized/redacted through control-plane result handling |
| Production key rotation | TODO: durable node credential rotation and agent auto-update hardening |

## Current Limitations

| Area | Status |
|---|---|
| Durable cloud repository adapters | TODO: Postgres/Redis adapters for the consolidated cloud repository ports |
| Agent process lifecycle | `CloudAgentApplication` boots a plain Java heartbeat/poll/execute/report loop; production daemon packaging is TODO |
| DNS provider integration | TODO port/interface |
| Object storage integration | TODO MinIO/S3 adapter |
| Kubernetes adapter | TODO optional adapter only |
| Disaster recovery runbooks | TODO docs/runbooks |
