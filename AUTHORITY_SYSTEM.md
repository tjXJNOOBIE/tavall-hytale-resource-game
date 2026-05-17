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

## Permission matrix

| Permission family | C1 | C2 | C3 | C4 | C5 | C6 |
|---|---|---|---|---|---|---|
| Workload read | Own node/workload | Regional workloads | Cross-region read | Cluster read | Global read | Global advisory read |
| Workload start/stop/restart | Own node/workload | Regional workloads | Failover preparation | Cluster maintenance | Global emergency ops | Recommend only |
| Workload delete | No | Scoped with approval | Scoped with approval | Cluster scoped approval | Global approval | No |
| Port/firewall/proxy apply | Own node only | Regional routing | Cross-region routing approval | Cluster routing policy | Global routing policy | Recommend only |
| Node registration | Own node join | Regional join approval | Region policy | Cluster admission policy | Global admission policy | No |
| Backup/restore | Own workload backup | Regional backup/restore | Cross-region restore approval | Cluster restore policy | Global restore approval | Recommend restore plan |
| LiveOps flags/rules | No | Region/dev scoped | Cross-region rollout review | Cluster/ecosystem rollout | Global rollout | Simulate/recommend only |
| Control commands | Local debug only | Regional ops commands | Failover/routing commands | Cluster lifecycle commands | Owner/global commands | Advisory commands |
| Secret rotation | No | No | No | No | Global approval | No |
| Destructive global ops | No | No | No | No | Explicit approval/break-glass policy | No |

## Scope examples

| Scope | Example target | Valid authority shape |
|---|---|---|
| Node | `node:oracle-arm-1` | C1 node agent or expiring break-glass human |
| Workload | `workload:minecraft-kingdom-1` | C1 owning node or C2 regional operator |
| Region | `region:us-west` | C2 regional operator or higher |
| Cluster | `cluster:resource-game-prod` | C4 cluster governor or C5 owner |
| Global | `global:tavall-cloud` | C5 owner, C6 read/recommend only |
| AI simulation | `simulation:failover-plan` | C6 advisory grant with no execution authority |

## Authorization flow

| Step | Check |
|---|---|
| 1 | Resolve principal and target resource scope. |
| 2 | Load command policy. |
| 3 | Load active authority grants for the principal. |
| 4 | Check level, permission, scope, approval, break-glass, and AI execution policy. |
| 5 | Record an authorization audit entry for allow or deny. |
| 6 | Only dispatch the command after validation and authorization pass. |

Implementation lives under `com.tavall.resourcegame.middleware.authority`. Control commands are wired through `ControlAuthorizationHandler` from the runtime factory.
