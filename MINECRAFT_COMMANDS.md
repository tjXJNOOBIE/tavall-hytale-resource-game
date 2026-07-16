# Minecraft Commands

Minecraft is an alpha platform alongside Discord. The control plane is the data and mutation source for kingdom commands; the proxy and Bukkit server plugins only collect context, submit command envelopes, render responses, and route players where their surface can do so safely.

## Command Surfaces

| Surface | Plugin/module | Commands | Owns | Does not own |
| --- | --- | --- | --- | --- |
| Velocity proxy | `minecraft-proxy` | `/server`, `/rank`, and Velocity routing only | Backend routing, instance switching transport, proxy-owned rank inspection/update surface | Kingdom command registration, Bukkit-only player/world state, scoreboard/actionbar/bossbar/inventory visuals |
| Bukkit/Paper server | `minecraft-game-server` | `/kd`, `/kd help`, `/kd ui`, `/kd data`, `/kd account`, `/kingdom`, `/tavallserver` | Kingdom command entrypoint, help menu, inventory GUI hub, player-data profile UI, server snapshots, world/player context, local visual rendering, server-console/direct-backend command forwarding | Canonical kingdom state |
| Control plane | Plain Java handlers imported by Minecraft modules; `control-server` is only an adapter/panel | Direct Java command pipeline | Command validation, command translation, authority checks, kingdom data, mutations, audit | Minecraft packet/UI rendering |

## Command Class Layout

Minecraft command handling is split by class, with a thin family router and one leaf class per exposed subcommand family. This matches the Minecraft-CTF style and keeps subcommand handling explicit.

| Root path | Router class | Leaf classes |
| --- | --- | --- |
| `/kd help` | `KingdomHelpCommand` | Chat help menu with command-family sections |
| `/kd ui` | `KingdomUiCommand` | `KingdomAdminGui`, `KingdomCastleGui`, `KingdomCitizensGui`, `KingdomTroopsGui`, `KingdomResourcesGui`, `KingdomAccountGui`, `KingdomBuildingsGui`, `KingdomBuildingGui`, `KingdomNpcGui`, `KingdomInteriorGui`, `KingdomPlacementGui`, `KingdomDebugGui` |
| `/kd building ...` | `KingdomBuildingCommand` | Focused building overview and command routing |
| `/kd npc ...` | `KingdomNpcCommand` | NPC interaction, building entry, and debug routing |
| `/kd data ...` | `KingdomDataCommand` | Player-data UI alias for the canonical control-plane profile view |
| `/kd account ...` | `KingdomAccountCommand` | Player-data UI and debug flow for the canonical control-plane profile view |
| `/kd companion ...` | `KingdomCompanionCommand` | `KingdomCompanionListCommand`, `KingdomCompanionGiveCommand`, `KingdomCompanionCreateCommand`, `KingdomCompanionDebugCommand`, `KingdomCompanionSetLevelCommand`, `KingdomCompanionXpCommand`, `KingdomCompanionMoraleCommand`, `KingdomCompanionBehaviorCommand`, `KingdomCompanionTrainCommand`, `KingdomCompanionClaimCommand`, `KingdomCompanionCancelCommand`, `KingdomCompanionSkillCommand`, `KingdomCompanionSummonCommand`, `KingdomCompanionRecallCommand`, `KingdomCompanionWallCommand`, `KingdomCompanionUiCommand`, `KingdomCompanionProjectionCommand` |
| `/kd castle ...` | `KingdomCastleCommand` | Family leaf class with forwarding and inventory routing |
| `/kd citizens ...` | `KingdomCitizensCommand` | Family leaf class with forwarding and inventory routing |
| `/kd resources ...` | `KingdomResourcesCommand` | Family leaf class with forwarding and inventory routing |
| `/rank ...` | `MinecraftVelocityRankCommand` | Proxy-owned universal rank inspection/update routed through the control plane |

## Proxy Routing

The remaining gameplay families - `building`, `npc`, `citizens`, `resources`, `companion`, `clock`, `hologram`, and `entity` - stay on the gameplay/server side. Only add new leaves when a live bot scenario proves a missing action, and keep the proxy surface limited to transport and permissions.

Velocity must not register `/kd` or `/kingdom`. Proxied player command input flows to the current Bukkit/Paper backend, where the server plugin owns command envelopes and can include server-only player/world context. Velocity now owns `/rank` for universal permission/rank inspection and update, with canonical rank state still living in the control plane. Velocity remains available for `/server kingdom` and future instance-switch transport after the backend/control plane requests a transfer.

Castle and building gameplay surfaces in Bukkit are represented by tagged in-world markers. Right-clicking those markers opens the relevant castle or building surface, and the protected structure regions are treated as unbreakable by normal player block interaction.

## Hytale KD Contract On Minecraft

The old Hytale `/kd` command list is now the shared kingdom command contract. Minecraft sends the same raw `/kd` input through imported Java modules; the backend either dispatches a canonical data command or routes the legacy surface category through `ROUTE_FRONTEND_KD_COMMAND`.

| Hytale-era command | Minecraft example | Backend route | Notes |
| --- | --- | --- | --- |
| `/kd ui [ui_type]` | `/kd ui castle` | `ROUTE_FRONTEND_KD_COMMAND` | Backend owns the route; Minecraft renders through server visuals when a visual response is available. |
| `/kd data` | `/kd data status` | `player debug <platformAccountId>` | Self data resolves from the control-plane identity/account path, and the Minecraft UI opens the same player profile view. `/kd data` is UI-only and does not forward other subcommands. |
| `/kd castle ...` | `/kd castle open` | `ROUTE_FRONTEND_KD_COMMAND` | Castle-local UI/teleport behavior is not mutated by Minecraft directly. |
| `/kd interior ...` | `/kd interior` | `ROUTE_FRONTEND_KD_COMMAND` | Instance/interior routing stays backend-owned. |
| `/kd citizens add <amount>` | `/kd citizens add 3` | `citizens spawn <platformAccountId> <amount> kingdom-1` | Hytale shorthand maps to the backend citizen command. |
| `/kd citizens set <amount>` | `/kd citizens set 12` | `ROUTE_FRONTEND_KD_COMMAND` | Set semantics need a durable backend command before mutation is allowed. |
| `/kd troops add\|set <amount>` | `/kd troops add 2` | `ROUTE_FRONTEND_KD_COMMAND` | Troop count mutation is not fabricated client-side. |
| `/kd resources add <type> <amount>` | `/kd resources add food 10` | `resource give <platformAccountId> <resourceAssetId> <amount>` | Supported resource aliases: `food`, `wood`, `iron`. |
| `/kd resources set <type> <amount>` | `/kd resources set food 50` | `ROUTE_FRONTEND_KD_COMMAND` | Set semantics need a durable backend command before mutation is allowed. |
| `/kd companion ...` | `/kd companion give ARCANE` | `companion give <platformAccountId> ARCANE` | The backend companion system owns canonical state. For player convenience, Minecraft `/kd companion` supports owner-less shorthands like `list`, `give`, `train`, `claim`, `cancel`, `summon`, `recall`, `skill`, and `wall assign/remove/debug` by injecting `platformAccountId`. |
| `/kd account status` | `/kd account status` | `account status <platformAccountId>` | Reads account progression and debug mode from the control plane and opens the player-data UI. |
| `/kd account addxp\|setlevel\|debug ...` | `/kd account addxp 10` | `account addxp <platformAccountId> 10` | Account progression and debug mode are stored in the control plane via `UniversalPlayerAccount` metadata. |
| `/kd ui account` | `/kd ui account` | `player debug <platformAccountId>` | Opens the same player-data UI as `/kd account` and `/kd data`, using the command center entrypoint. |
| `/kd buildings ...` | `/kd buildings list` | `ROUTE_FRONTEND_KD_COMMAND` | Building lifecycle commands keep the Hytale command shape for Minecraft. |
| `/kd building ...` | `/kd building open` | `ROUTE_FRONTEND_KD_COMMAND` | Focused building page and controller alias for the Minecraft UI. |
| `/kd npc ...` | `/kd npc open` | `ROUTE_FRONTEND_KD_COMMAND` | NPC interaction page and building handoff alias for the Minecraft UI. |
| `/kd nodes ...` | `/kd nodes list` | `ROUTE_FRONTEND_KD_COMMAND` | Node commands keep the Hytale command shape for Minecraft. |
| `/kd place ...` | `/kd place castle` | `ROUTE_FRONTEND_KD_COMMAND` | Placement requires server/world context and backend authorization. |
| `/kd focus`, `/kd interact`, `/kd scan` | `/kd scan` | `ROUTE_FRONTEND_KD_COMMAND` | Frontend interaction input is forwarded, not mutated locally. |
| `/kd hologram ...`, `/kd entity ...` | `/kd hologram status` | `ROUTE_FRONTEND_KD_COMMAND` | Visual/entity diagnostics remain backend-routed for parity. |
| `/kd bootstrap`, `/kd scene refresh`, `/kd tutorial reset` | `/kd scene refresh` | `ROUTE_FRONTEND_KD_COMMAND` | Minecraft can issue the old command, and backend decides what state/visual refresh is valid. |
| `/kd tick run [count]` | `/kd tick run 1` | `tick healing <count>` | Existing backend tick command path. |
| `/rank list|inspect|set` | `/rank list`, `/rank inspect miner-1`, `/rank set miner-1 moderator` | `RankApi` via control-plane TCP bridge | Proxy-owned universal permission rank surface; control plane remains canonical. |

## Server Commands

Use these from proxied gameplay, the backend server console, direct backend test connections, or server-local diagnostics. `/kd` and `/kingdom` are registered only on Bukkit/Paper for Minecraft alpha.

| Command | Example | Java pipeline path | Surface requirement | Notes |
| --- | --- | --- | --- | --- |
| `/kd <command...>` | `/kd clock state kingdom-1` | Direct command dispatch | Bukkit/Paper | Server-side `/kd` forwards a Minecraft command envelope with `surfaceIdentity=BUKKIT_SERVER`. It does not mutate local state. |
| `/kingdom <command...>` | `/kingdom citizens summary kingdom-1` | Direct command dispatch | Bukkit/Paper | Alias for `/kd` on the server surface. |
| `/tavallserver snapshot` | `/tavallserver snapshot` | Direct snapshot handler | Bukkit/Paper | Sends player/world/runtime data that Velocity cannot read. |
| `/tavallserver visual <chat|title> <message>` | `/tavallserver visual title Server visual path online` | No | Bukkit/Paper player | Renders a local diagnostic visual through the server plugin. |
| `/tavallserver interact <message>` | `/tavallserver interact diagnostic server event` | Direct command dispatch | Bukkit/Paper player | Converts a frontend interaction into a backend interaction request envelope. |

## Surface Selection Rules

| Command need | Use proxy | Use server |
| --- | --- | --- |
| Global kingdom read/mutation | No | Yes |
| Rank inspection/update | Yes | No |
| Permission pre-check before player command | No | Minimal Bukkit permission only; control plane still decides canonical behavior |
| Player transfer between backend servers | Yes | No |
| Backend player list, world name, coordinates, health-like runtime data | No | Yes |
| Scoreboard/actionbar/bossbar/chat/inventory visual render | No | Yes |
| Backend interaction event from world/player context | No | Yes |
| Control-plane data mutation | Submit only | Submit only |

## Bukkit Event Handling

The server plugin follows the Minecraft-CTF event shape: each raw Bukkit event has one dedicated listener dependency, and the plugin entrypoint only registers those listeners. This keeps command forwarding, visual rendering, and snapshot submission from racing across multiple handlers of the same event type.

| Bukkit event | Handler | Event options | Backend behavior |
| --- | --- | --- | --- |
| `PlayerJoinEvent` | `MinecraftBukkitPlayerJoinHandler` | `EventPriority.MONITOR` | Renders the local join visual, then schedules snapshot submission through `IMinecraftBukkitTaskScheduler`. |
| `PlayerInteractEntityEvent` | `MinecraftBukkitInteractionHandler` | `EventPriority.MONITOR`, `ignoreCancelled=true` | Submits one backend interaction envelope for uncancelled entity interactions. |

## Visual Feedback Families

Minecraft server feedback now uses in-world cues instead of prototype/test wording. The server plugin maps command families to titles, sounds, particles, and local block cues when the command succeeds, while still showing a concise Kingdom chat line.

| Command family | Visual response |
| --- | --- |
| `companion` | Companion-join, training, summon, recall, and wall-order cues use titles, sounds, and particles. |
| `castle` | Castle commands use stone and beacon-style block pulses with a castle-order title. |
| `citizens` | Citizen commands use villager audio and happy-villager particles. |
| `resources` | Resource changes use pickup sounds and glowstone-style pulses. |
| `clock`, `schedule`, `aging` | Time and schedule changes use bell tones and end-rod cues. |
| `kingdom`, `ui`, `scene`, `bootstrap` | Kingdom surface refreshes use amethyst and enchant-style feedback. |
| `nodes`, `buildings`, `building`, `place` | Placement and structure changes use iron-block and beacon cues. |
| `npc`, `interior`, `focus`, `interact`, `scan` | Interaction and interior flows use portal-style cues. |

## Inventory UI Assets

The Bukkit inventory UI now carries Hytale asset references in the page header item so the Minecraft surface stays aligned with the Hytale UI stack. `/kd ui` opens the command center page and then branches into the castle, citizen, troop, resource, building, interior, placement, and debug inventory pages.

| Minecraft inventory page | Hytale asset references |
| --- | --- |
| `DEBUG_NAVIGATOR` | `ui_panel_command_center_base`, `ui_icon_help_book`, `ui_icon_kingdom_castle` |
| `CASTLE_MAIN` | `ui_panel_castle_ledger_base`, `ui_icon_kingdom_castle` |
| `CASTLE_TROOPS` | `ui_panel_war_table_base`, `ui_icon_population_troop` |
| `CASTLE_BUILDINGS` | `ui_panel_workshop_base`, building icons |
| `RESOURCE_NODE_DETAIL` | `ui_panel_node_detail_base`, `ui_icon_node_marker` |
| `NPC_MAIN` | `ui_panel_npc_detail_base`, `ui_icon_population_worker` |
| `BUILDING_DETAIL` | `ui_panel_workshop_base`, `ui_icon_action_move`, `ui_icon_action_upgrade`, `ui_icon_action_storage` |
| `INTERIOR_MAIN` | `ui_panel_interior_base` |
| `DEBUG_PLACEMENT` | `ui_selector_building_valid`, `ui_selector_corner_valid`, `ui_selector_radius_ring` |

## Alpha Scope

| Platform | Alpha status | Notes |
| --- | --- | --- |
| Minecraft | Active | Velocity plus Bukkit/Paper are first-release targets. |
| Discord | Active | Discord commands share the same control-plane command ingress model. |
| Hytale | Deferred for alpha | Keep existing code compiling, but do not spend alpha implementation/testing time here unless it blocks shared Minecraft/Discord contracts. |
| Roblox and other platforms | Deferred for alpha | Do not continue feature work for this release window. |
