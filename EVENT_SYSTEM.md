# Backend Game Event System

Frontend events are interaction input only. Backend game events are the only path for game logic mutation.

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

| Category | Event examples |
|---|---|
| Castle | CastleCreatedEvent, CastleSelectedEvent, CastleInteractionOpenedEvent, CastleInteriorEnterRequestedEvent, CastleInteriorEnteredEvent, CastleMovedEvent, CastleVisualUpdatedEvent |
| Citizen | CitizenCreatedEvent, CitizenAgedEvent, CitizenJobAssignedEvent, CitizenPromotedToTroopEvent, TroopDemotedToCitizenEvent, CitizenMoraleChangedEvent, CitizenDiedEvent |
| Resource | ResourceAddedEvent, ResourceRemovedEvent, ResourceSetEvent, ResourceThresholdReachedEvent, ResourceShortageEvent |
| Companion | CompanionCreatedEvent, CompanionSummonedEvent, CompanionRecalledEvent, CompanionTrainingStartedEvent, CompanionTrainingCompletedEvent, CompanionSkillUnlockedEvent, CompanionSkillUpgradedEvent, CompanionBehaviorChangedEvent |
| Clock | KingdomClockTickEvent, KingdomClockPhaseChangedEvent, KingdomDayStartedEvent, KingdomNightStartedEvent, ScheduleWindowStartedEvent, ScheduleWindowEndedEvent |
| LiveOps | GameRuleChangedEvent, FeatureFlagChangedEvent, SystemToggleChangedEvent, ItemVaultedEvent, ItemUnvaultedEvent, LiveConfigReloadedEvent, GlobalGuiUpdatedEvent |
| Distributed | NodeRegisteredEvent, NodeHeartbeatEvent, NodeStatusChangedEvent, RemoteCommandExecutedEvent, DistributedEventForwardedEvent, RemoteSmokeTestCompletedEvent |

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
