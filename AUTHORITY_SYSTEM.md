# C1-C6 Authority System

The control plane uses scoped authority instead of server-name authority. A principal receives an authority level, scope, permissions, and conditions. C6 is intelligence/meta-authority, not owner authority.

## Authority levels

| Level | Name | Primary use | Can approve destructive human ops |
|---|---|---|---|
| C1 | Local executor | One node or narrow workload scope | No |
| C2 | Regional operator | Region-local node group and workloads | Scoped approvals |
| C3 | Regional supervisor | Cross-region comparison, routing, failover approval | Cross-region only |
| C4 | Cluster governor | Cluster, shard, or ecosystem lifecycle | Cluster scope |
| C5 | Global authority | Owner/global operational authority | Yes |
| C6 | Intelligence authority | AI recommendation, simulation, guarded automation | No, requires C5 policy/approval |

## Principal types

| Principal | Example | Default shape |
|---|---|---|
| HUMAN_ADMIN | TJ | C5 global owner authority |
| SERVICE_ACCOUNT | deployer | Scoped command execution |
| NODE_AGENT | oracle-arm-1 agent | C1 node authority |
| BOT_TEST_RUNNER | smoke runner | C1/C2 test scope |
| AI_AGENT | optimizer | C6 advisory/global read |
| SYSTEM_SCHEDULER | regional scheduler | C2 region authority |
| EMERGENCY_FALLBACK | local recovery agent | Expiring C1 break-glass authority |

## Command policy examples

| Command type | Minimum level | Permission | Approval | Break-glass | AI execution |
|---|---:|---|---|---|---|
| START_WORKLOAD | C1 | WORKLOAD_START | No | No | Allowed |
| DELETE_WORKLOAD | C2 | WORKLOAD_DELETE | Yes | No | Not allowed |
| REGION_FAILOVER | C3 | REGION_FAILOVER_EXECUTE | Yes | No | Not allowed |
| CLUSTER_DESTROY | C4 | CLUSTER_DESTROY | Yes | No | Not allowed |
| GLOBAL_SECRET_ROTATE | C5 | GLOBAL_SECRET_ROTATE | Yes | No | Not allowed |
| GLOBAL_FAILOVER | C5 | GLOBAL_DISASTER_RECOVERY_EXECUTE | Yes | Yes | Not allowed |
| AI_RECOMMEND_ACTION | C6 | AI_RECOMMEND | No | No | Allowed |

## Authorization flow

| Step | Check |
|---|---|
| 1 | Resolve principal and target resource scope. |
| 2 | Load command policy. |
| 3 | Load active authority grants for the principal. |
| 4 | Check level, permission, scope, approval, break-glass, and AI execution policy. |
| 5 | Record an authorization audit entry for allow or deny. |
| 6 | Only dispatch the command after validation and authorization pass. |

Implementation lives under `com.tavall.hytale.resourcegame.middleware.authority`. Control commands are wired through `ControlAuthorizationHandler` from the runtime factory.
