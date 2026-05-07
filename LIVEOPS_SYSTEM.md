# LiveOps Control Plane

LiveOps is the runtime game director: feature flags, game rules, toggles, balance values, global GUI definitions, and scheduled modifiers without redeploying.

## Config layers

| Layer | Use |
|---|---|
| Postgres | Durable config source |
| Redis | Broadcast/cache for config updates |
| Memory | Hot runtime registry |

## Config types

| Type | Example keys |
|---|---|
| FEATURE_FLAG | ui.debug.enabled, companions.enabled |
| GAME_RULE | castle.interior.enabled, events.kingdomClock.enabled |
| BALANCE_VALUE | resources.food.generationMultiplier |
| ITEM_TOGGLE | companions.skill.arcaneLance.enabled |
| RESOURCE_TOGGLE | resources.iron.generationMultiplier |
| SYSTEM_TOGGLE | castle.relocation.enabled, citizens.aging.enabled |
| UI_DEFINITION | gui.castle.main, gui.liveops.main |
| EVENT_TOGGLE | events.resourceShortage.enabled |
| COMMAND_TOGGLE | debug.commands.enabled, remote.testing.enabled |
| SCHEDULED_MODIFIER | double resources for timed windows |

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
