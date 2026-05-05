# Kingdom Clock System

## Architecture
The canonical Kingdom Clock lives in the plain Java control plane, not in Spring Boot and not in any frontend adapter.

- `com.tavall.hytale.resourcegame.middleware.clock.KingdomClockControlSystem` owns clock state, config, phase calculation, schedule rules, aging ticks, and projections.
- The optional Spring web panel calls the Java runtime and command dispatcher directly through `/control/clock`.
- Hytale, Minecraft, Roblox, Discord, Android, and PC clients consume projected clock/schedule data and may render visuals from it, but they do not decide canonical time or schedule effects.
- The older Hytale `KingdomClockService` remains an adapter-side world-time applier. It should be driven by control-plane projections/snapshots, not treated as the canonical cross-platform clock.

## Supported Modes
- `REAL_TIME_SYNCED`: follows server/configured timezone.
- `ACCELERATED`: advances canonical kingdom minutes by a configurable multiplier.
- `FIXED_OVERRIDE`: pins a kingdom to a test/debug time such as `22:00`.
- `PAUSED`: freezes canonical state.

Default phase windows are:
- `DAWN`: 05:00-06:59
- `DAY`: 07:00-17:59
- `DUSK`: 18:00-20:59
- `NIGHT`: 21:00-04:59

## Schedule Backbone
Schedule rules use `KingdomClockState` and support midnight-wrapping windows. Current hooks cover citizen work/sleep state, shop open hints, interior lights/mood, troop training modifiers, building productivity modifiers, morale penalties, night-event eligibility, and aging ticks.

The immediate implementation is intentionally a backbone. Downstream citizen AI, raids, holidays, laws, taxes, and production persistence should subscribe/read these states instead of adding local clock rules.

## Commands
All mutations go through the control command pipeline:

- `clock state <kingdomId>`
- `clock tick <kingdomId>`
- `clock tick-all`
- `clock mode <kingdomId> <REAL_TIME_SYNCED|ACCELERATED|FIXED_OVERRIDE|PAUSED>`
- `clock override <kingdomId> <HH:mm>`
- `clock clear-override <kingdomId>`
- `clock pause <kingdomId>`
- `clock resume <kingdomId>`
- `clock config <kingdomId> key=value ...`
- `schedule active <kingdomId>`
- `schedule create <kingdomId> <ruleType> key=value ...`
- `schedule enable <ruleId>`
- `schedule disable <ruleId>`
- `schedule apply <kingdomId>`
- `aging policy <kingdomId> key=value ...`
- `aging tick <kingdomId>`

The `/kd clock`, `/kd schedule`, and `/kd aging` frontend command categories translate into the same canonical command parser.

## Verification Notes
- Java control-plane tests cover phases, modes, windows, schedule hooks, aging, command parsing/dispatch, projections, and the optional Spring clock page.
- Minecraft/Hytale/Roblox/Discord projection tests run in memory through shared `FrontendProjectionHandler` wrappers.
- Hytale live bot testing should use the exact `C:\Users\TJ\Documents\HytaleDevServer` folder mirrored 1:1 to the remote `HytaleDevServer` path. The bot harness should run on the remote beside that server and verify `clock override kingdom-1 22:00` updates Hytale projection state to `NIGHT`.

## TODO
- Add durable persistence/migrations for `KingdomClockState`, `KingdomClockConfig`, `KingdomScheduleRule`, `AgingTickPolicy`, and last aging tick state.
- Add seasons, holidays/festivals, laws/tax schedules, and player-local display formatting.
- Wire full citizen AI, full raid/event windows, and production job scheduling to these clock hooks.
- Add live Hytale world visual sync from canonical projections rather than adapter-local time calculation.
- Add production instance/broadcast loop for visual projection refreshes.
