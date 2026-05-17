# Backend Game Event System

Frontend events are interaction input only. Backend game events are the only path for game logic mutation.

## Current runtime boundary

| Runtime surface | Owns | Does not own |
|---|---|---|
| `ResourceGamePlugin` in core | Core game event registration, event dispatch, debug command registration, canonical gameplay mutations | Proxy commands, Bukkit visuals, direct Minecraft UI rendering |
| `MinecraftBukkitServerPlugin` | Bukkit listener registration, local visual response, server-side command forwarding, inventory UI clicks | Canonical backend state mutation without backend dispatch |
| Velocity proxy plugin | Proxy routing and `/rank` only | `/kd`, `/kingdom`, and gameplay event listeners |
| Control plane API | Canonical player/rank/data reads and writes | Bukkit/Paper or Velocity API usage |

## Event flow

| Stage | Responsibility |
|---|---|
| Frontend interaction | Click/hover/button/select input only |
| InteractionBridge | Converts input to backend request |
| Backend GameEvent | Typed real game logic event |
| Middleware pipeline | Logging, validation, permission, rate limit, audit, debug, cancellation |
| Event handler | System-specific reaction |
| Mutation pipeline | State update and dirty sync |
| UI refresh | Visual state changes after backend success |

## Event categories

| Category | Event examples | Normal source | Mutation owner | Projection/UI effect |
|---|---|---|---|---|
| Castle | CastleCreatedEvent, CastleSelectedEvent, CastleInteractionOpenedEvent, CastleInteriorEnterRequestedEvent, CastleInteriorEnteredEvent, CastleMovedEvent, CastleVisualUpdatedEvent | Join bootstrap, `/kd castle`, interaction bridge | Castle services | Castle pages, markers, interior links |
| Citizen | CitizenCreatedEvent, CitizenAgedEvent, CitizenJobAssignedEvent, CitizenPromotedToTroopEvent, TroopDemotedToCitizenEvent, CitizenMoraleChangedEvent, CitizenDiedEvent | Control command, clock tick, training UI | Citizen/population services | Citizen summaries, display anchors, troop rows |
| Resource | ResourceAddedEvent, ResourceRemovedEvent, ResourceSetEvent, ResourceThresholdReachedEvent, ResourceShortageEvent | Economy tick, command, node collection | Resource service | Resource counters, shortage alerts |
| Companion | CompanionCreatedEvent, CompanionSummonedEvent, CompanionRecalledEvent, CompanionTrainingStartedEvent, CompanionTrainingCompletedEvent, CompanionSkillUnlockedEvent, CompanionSkillUpgradedEvent, CompanionBehaviorChangedEvent | Companion UI, schedule/tick | Companion service | Companion panels and world presence |
| Clock | KingdomClockTickEvent, KingdomClockPhaseChangedEvent, KingdomDayStartedEvent, KingdomNightStartedEvent, ScheduleWindowStartedEvent, ScheduleWindowEndedEvent | Clock runtime, CLI/web control command | Kingdom clock service | Day/night state, schedule projections |
| LiveOps | GameRuleChangedEvent, FeatureFlagChangedEvent, SystemToggleChangedEvent, ItemVaultedEvent, ItemUnvaultedEvent, LiveConfigReloadedEvent, GlobalGuiUpdatedEvent | LiveOps mutation handler | LiveOps registry | Runtime toggles, GUI reloads |
| Distributed | NodeRegisteredEvent, NodeHeartbeatEvent, NodeStatusChangedEvent, RemoteCommandExecutedEvent, DistributedEventForwardedEvent, RemoteSmokeTestCompletedEvent | Node agent, distribution probe, remote runner | Distribution/cloud handlers | Control panel health and smoke artifacts |

## Input event sources

| Source | Event path | Notes |
|---|---|---|
| Player login/logout | `PlayerReadyEvent`, `PlayerDisconnectEvent` | Core player lifecycle and session state only. |
| Player interaction | `PlayerInteractEvent` | Routed to placement, castle, resource-node, building, NPC, and custom-entity handlers in core. |
| Bukkit entity interaction | `PlayerInteractEntityEvent` | Server frontend visual/interaction input only. |
| Inventory click | Bukkit inventory click events | Server frontend UI routing only. |
| Proxy permission command | `/rank` | Proxy-owned command surface, but rank state still lives in the control plane API. |

## Event result contract

| Field | Required behavior |
|---|---|
| `success` | True only when validation, permission, and handler mutation completed. |
| `cancelled` | True when middleware intentionally stopped dispatch before mutation. |
| `emittedEvents` | Follow-up typed events, never raw frontend input. |
| `dirtyFields` | Canonical state or projection keys that need refresh. |
| `metadata` | Debug/audit context, no secrets or private credentials. |
| `message` | Operator-readable result for CLI/web/bot logs. |

## Middleware table

| Middleware | Purpose |
|---|---|
| EventLoggingMiddleware | Records event receipt and result |
| EventPermissionMiddleware | Checks actor/source permission |
| EventRateLimitMiddleware | Blocks repeated unsafe dispatch |
| EventValidationMiddleware | Validates event payload |
| EventAuditMiddleware | Writes durable action trace |
| EventDebugMiddleware | Adds debug metadata when enabled |
| EventDispatchMetricsMiddleware | Tracks dispatch timing/counts |
| EventCancellationMiddleware | Allows policy cancellation before mutation |
| EventDistributedForwardingMiddleware | Publishes safe events to distributed nodes |

Events should keep returning `GameEventResult` with success, cancellation, emitted events, dirty fields, and metadata.

## DI boundary

| Component | Dependency rule |
|---|---|
| `GameEventDependencyModule` | Registers the default event pipeline in Tavall DI |
| `IGameEventDomainGenerated` | Exposes default accessors used directly by handlers |
| `GameEventDispatchHandler` | Reads middleware and listeners from DI at dispatch time |
| `InteractionBridgeHandler` | Reads action-to-event mappings and dispatcher from DI |
| Middleware handlers | Use default accessors instead of constructor-injected collaborators |

Redis subscriber/forwarder adapters remain external-boundary classes because their Jedis/channel objects come from deployment configuration.
