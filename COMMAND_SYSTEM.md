# Command System

## Purpose
Provide a development-first admin and debug surface that accelerates gameplay iteration and bot testing without hard-coding one-off server logic into page classes.

## Main command root
- `/kingdom`
- alias: `/kd`

## Current command families
- `ui`
- `data`
- `castle`
- `interior`
- `citizens`
- `troops`
- `resources`
- `account`
- `hologram`
- `nodes`
- `place`
- `focus`
- `interact`
- `scan`
- `scene`
- `bootstrap`
- `tick`
- `tutorial`

## Main classes
- `DebugCommandService`
- `KingdomCommand`
- `KingdomPlacementCommandSupport`
- `KingdomNodeCommandSupport`
- `KingdomInteractionCommandSupport`

## Bot/admin-specific helpers
- `/kd place castle`
- `/kd place node <type>`
- `/kd place confirm`
- `/kd place cancel`
- `/kd place status`
- `/kd focus`
- `/kd interact`
- `/kd scan`
- `/kd castle goto`
- `/kd castle align`
- `/kd castle move`
- `/kd nodes goto <index|node_id_prefix>`
- `/kd nodes align <index|node_id_prefix|focus>`
- `/kd nodes status <index|node_id_prefix|focus>`
- `/kd scene refresh`
- `/kd bootstrap`
- `/kd tick run [count]`
- `/kd tutorial reset`
- `/kd account debug on|off|status [player_name|uuid_prefix]`
- `/kd hologram spawn <text>`
- `/kd hologram stack <line1|line2|...>`
- `/kd hologram status`
- `/kd hologram clear`

## Design notes
- Commands are intentionally grouped by system instead of dumping every mutation into one flat namespace.
- Focus and interact commands exist so bots can behave more like players instead of opening everything through direct command-only shortcuts.
- Placement commands exist so bots can use the same server-side targeting logic as real players until native click packets are integrated.
- Mutation commands refresh tracked castle/resource/node pages when possible so live operator sessions and bot scenarios can observe state changes without manual page teardown.
- Command handlers should mutate services, not repositories or raw metadata directly.
## Account Progression Commands
- `/kd account status [player_name|uuid_prefix]` prints the account level, current XP, required XP, total XP, and debug restriction state.
- `/kd account addxp <amount> [player_name|uuid_prefix]` grants account XP and carries overflow through as many levels as needed.
- `/kd account setlevel <level> [player_name|uuid_prefix]` is a debug/admin shortcut for validating building unlocks and interior building lots.
- `/kd account debug on|off|status [player_name|uuid_prefix]` persists a debug mode flag that lets the targeted player ignore account-level building restrictions during testing.

## Hologram Debug Commands
- `/kd hologram spawn <text>` spawns one nameplate-backed test label above the player.
- `/kd hologram stack <line1|line2|...>` spawns multiple label lines; empty pipe sections are ignored.
- `/kd hologram status` reports the active hologram refs owned by the player.
- `/kd hologram clear` removes the player's active debug holograms.
- Hologram spawns log model resolution, nameplate-only fallback use, successful refs, and spawn/removal failures so console output explains why a label did not appear.

## Middleware Control Plane
- CLI entry point: `com.tavall.hytale.resourcegame.controlserver.cli.ControlConsoleApplication`.
- Spring Boot control panel entry point: `com.tavall.hytale.resourcegame.controlserver.web.ControlServerApplication`, with pages rooted at `/control`.
- Both surfaces delegate to `ControlCommandDispatchHandler`; neither the CLI nor the web controllers mutate gameplay state directly.
- Current command batch includes `DEBUG_PLAYER_STATE`, `REGISTER_GLOBAL_ASSET`, `REFRESH_FRONTEND_PROJECTIONS`, `ASSIGN_TROOP_WOUND`, `START_TROOP_HEALING`, `RUN_HEALING_TICK`, `GIVE_RESOURCE`, `BROADCAST_PLATFORM_MESSAGE`, `SYNC_PLATFORM_STATE`, `DEBUG_TROOP_HEALING_STATE`, and the universal kingdom commands for kingdom creation/scaling, borders, coordinates, player locations, instance routing, editable parameters, and new-player routing.
- Useful CLI examples:
  - `dry-run troop wound <troopId> GENERAL_WOUND MODERATE`
  - `execute troop wound <troopId> GENERAL_WOUND MODERATE`
  - `resource give <universalPlayerId> item.healing.field_rations 20`
  - `troop heal treatment <universalPlayerId> <troopId> recipe.healing.general_wound.proper 4`
  - `tick healing 1`
  - `projection refresh minecraft`
  - `platform sync all`
  - `kingdom create --displayName First --worldId default --borderSize 1000`
  - `kingdom debug kingdom-1`
  - `kingdom scaling evaluate`
  - `kingdom border resolve default 100 65 100`
  - `kingdom border simulate-crossing player-1 default 100 65 100 1200 65 100`
  - `coord convert roblox default 10 0 20`
  - `coord params roblox platformXAxisCanonicalAxis=x platformYAxisCanonicalAxis=z platformZAxisCanonicalAxis=y platformScaleX=1`
  - `instance register minecraft kingdom-1 kingdom-1-minecraft-primary`
  - `instance health kingdom-1-minecraft-primary ONLINE`
  - `instance switch player-1 minecraft kingdom-1 kingdom-2`
  - `instance routing debug kingdom-1 minecraft`
  - `params list`
  - `params get maxActivePlayers kingdom-1`
  - `params set maxActivePlayers 300 KINGDOM kingdom-1`
  - `params dry-run maxActivePlayers 0 KINGDOM kingdom-1`
  - `broadcast "message"`
- Dry-run commands validate permissions and arguments and calculate intended object/platform effects without mutating middleware state or sending platform fanout.
- Command results and audit logs are repository-backed; the current runtime uses in-memory repositories for tests/local control-server boot and `004_cross_platform_middleware.sql` defines the Postgres table shape for production adapters.
- Postgres-backed repositories now exist for control command results, audit logs, operators, platform fanout retry records, and scheduled commands.
- Failed platform fanout creates retry records instead of silently disappearing; scheduled commands dispatch through the same `ControlCommandDispatchHandler` as CLI and web input.
- Minecraft and Hytale verification should start from the shared control pipeline, then use platform debug/projection hooks to observe wound/healing projection changes. Roblox and Discord are verified through in-memory fanout/projection adapters until live runtimes are wired.
- Universal kingdom verification should start in the plain Java control runtime. Minecraft/Hytale/Roblox location updates are platform coordinates, the backend converts to canonical coordinates, border containment resolves the kingdom, and any cross-border transition creates an instance-switch request for the frontend adapter to execute.
