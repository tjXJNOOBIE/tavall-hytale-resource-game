# LiveOps Control Plane

LiveOps is the runtime game director: feature flags, game rules, toggles, balance values, global GUI definitions, and scheduled modifiers without redeploying.

## Config layers

| Layer | Use |
|---|---|
| Postgres | Durable config source |
| Redis | Broadcast/cache for config updates |
| Memory | Hot runtime registry |

## Config types

| Type | Example keys | Value shape | Expected reader |
|---|---|---|---|
| FEATURE_FLAG | `ui.debug.enabled`, `companions.enabled` | Boolean | UI/navigation and feature entry checks |
| GAME_RULE | `castle.interior.enabled`, `events.kingdomClock.enabled` | Boolean or enum/string | Gameplay services |
| BALANCE_VALUE | `resources.food.generationMultiplier` | Number/string | Economy/resource calculators |
| ITEM_TOGGLE | `companions.skill.arcaneLance.enabled` | Boolean | Item/skill unlock checks |
| RESOURCE_TOGGLE | `resources.iron.generationMultiplier` | Boolean/number | Resource generation services |
| SYSTEM_TOGGLE | `castle.relocation.enabled`, `citizens.aging.enabled` | Boolean | System-level handlers |
| UI_DEFINITION | `gui.castle.main`, `gui.liveops.main` | JSON definition | Global GUI registry |
| EVENT_TOGGLE | `events.resourceShortage.enabled` | Boolean | Event dispatcher/listeners |
| COMMAND_TOGGLE | `debug.commands.enabled`, `remote.testing.enabled` | Boolean | Command ingress/dispatch |
| SCHEDULED_MODIFIER | double resources for timed windows | Rule plus time window | Clock/schedule readers |

## Toggle matrix

| Toggle | Disabled behavior | Enabled behavior | Audit/event |
|---|---|---|---|
| `castle.interior.enabled` | Interior entry requests fail cleanly with a player/operator message | Castle interior request emits entry events and teleports when valid | GameRuleChangedEvent |
| `castle.relocation.enabled` | Castle move commands dry-run/deny mutation | Castle move command mutates castle position and visuals | SystemToggleChangedEvent |
| `citizens.aging.enabled` | Aging tick records skipped/no-op result | Aging tick updates citizen age state | SystemToggleChangedEvent |
| `events.kingdomClock.enabled` | Clock event fanout is skipped; direct state reads remain available | Clock tick/phase events dispatch normally | GameRuleChangedEvent |
| `events.resourceShortage.enabled` | Shortage event listener does not emit player-facing alert | Resource shortage events create alert/projection updates | FeatureFlagChangedEvent |
| `debug.commands.enabled` | Debug command families are rejected at ingress | `/kd` debug families dispatch by authority | FeatureFlagChangedEvent |
| `remote.testing.enabled` | Remote bot/smoke commands are rejected | Remote harness command families are available | FeatureFlagChangedEvent |
| `ui.debug.enabled` | Debug navigator hidden/unavailable | Debug UI pages can open for authorized users | GlobalGuiUpdatedEvent |

## Rollout strategies

| Strategy | Scope |
|---|---|
| GLOBAL | All online nodes |
| NODE_TYPE | Specific runtime class |
| NODE_ID | One node |
| PLAYER_PERCENTAGE | Gradual user rollout |
| PLAYER_ALLOWLIST | Explicit users |
| KINGDOM_ID | One kingdom |
| DEV_ONLY | Development-only changes |

## Update flow

| Step | Action |
|---|---|
| 1 | Admin command or GUI submits config change |
| 2 | Validate change and required authority |
| 3 | Persist `LiveConfigEntry` to Postgres |
| 4 | Publish Redis update |
| 5 | Nodes reload memory registry |
| 6 | Fire GameRuleChangedEvent, FeatureFlagChangedEvent, or GlobalGuiUpdatedEvent |
| 7 | Systems/UI react on next read/open |

Every major system should call the live config registry or toggle handler and fail cleanly with an audit/debug event when disabled.

## DI boundary

| Component | Dependency rule |
|---|---|
| `LiveOpsDependencyModule` | Registers in-memory defaults and preserves pre-registered production adapters |
| `LiveOpsDomain` | Exposes repository, registry, publisher, mapper, and optional event dispatcher accessors |
| `LiveConfigMutationHandler` | Mutates config through DI-resolved repository/registry/publisher |
| `GlobalGuiMutationHandler` | Mutates GUI definitions through DI-resolved repository/registry/publisher |
| Toggle/rule handlers | Read the live config registry through default accessors |

Redis and Postgres LiveOps classes are adapter boundaries. The mutation path stays decoupled so the optional Spring panel can call Java handlers directly without owning state.
