# Architecture Summary

## Core Systems
- ResourceGamePlugin now boots through a repo-local Tavall-style DI composition root and resolves runtime services through `IResourceGameDomain`.
- PlayerDataService hydrates PlayerProfile + PlayerGameState via Redis-first cache and Postgres fallback.
- CastleSpawnService spawns the placeholder castle entity and tracks it in CastleEntityRegistry.
- CastleInteractionService listens for near/look interactions and opens the castle UI.
- InteriorWorldService handles enter/exit flows using same-server interior coordinates.
- InteriorWorldService and InteriorInstanceService place each player into a dedicated same-process interior world for clean enter/exit behavior.
- PopulationService manages citizen/troop continuum and promotion/demotion rules.
- PopulationDisplayService spawns anchored NPCs that show counts in the interior.
- ResourceService mutates Food/Wood/Iron inventory.
- KingdomClockService provides 24-hour day/night state.

## UI
- Custom .ui pages live under Common/UI/Custom/Pages.
- UiPageRegistry + UiNavigator build and open pages.
- UiActionService routes button actions to game services and now clears first-join tutorial milestones when upgrade actions succeed.
- DebugNavigatorPage exposes cache mode, persistence mode, and onboarding milestone state for testable operator visibility.
- InteriorMainPage and CastleUpgradesPage surface first-join tutorial copy from persisted onboarding metadata.

## Dependency Composition
- `dependency/` contains a repo-local compatibility layer that mirrors the shared Tavall token/domain access pattern while the upstream `tavall-di` module remains non-buildable in this monorepo.
- `ResourceGameDependencyModule` is the single composition root for service registration.
- Runtime-facing services resolve through interfaces first so the repo can swap to the shared DI package later with a smaller migration.

## Persistence
- PlayerProfileRepository and PlayerGameStateRepository use explicit Postgres tables.
- Semantic cache (hot memory + Redis) is used for read-through caching.
- AsyncTask is used for all persistence writes off the main thread.

## Cross-Platform Middleware
- The canonical game brain now lives under `src/main/java/com/tavall/hytale/resourcegame/middleware`.
- Universal player IDs are canonical; Minecraft, Hytale, Roblox, and Discord account IDs are platform bindings only.
- Guilds, castles, nodes, treasury/taxes, petitions, propaganda, troops, trade routes, assets, 2FA, troop wounds, healing recipes, healing resources, healing facilities, and projections are modeled as middleware state and handlers.
- Platform projection handlers translate canonical objects into Minecraft, Hytale, Roblox, and Discord representations without mutating gameplay state.
- Global asset IDs are canonical; platform asset versions map back to the same global asset and fall back by global ID when a platform asset is missing.
- Game-domain behavior uses `Handler` classes; platform adapters remain thin render/input translators.

## Control Plane
- The middleware/control server now has a shared `ControlCommandDispatchHandler` pipeline for CLI and Spring MVC control-panel inputs.
- CLI commands, web-panel forms, and future API inputs parse once into `ControlCommand`, validate permissions/dry-run policy, mutate only canonical middleware state, and then fan out projection refresh/control events to Minecraft, Hytale, Roblox, and Discord adapters.
- Control operators use explicit roles and permissions; high-risk commands require ADMIN, OWNER, or SYSTEM policy hooks, and all accepted/rejected/dry-run commands are audit logged with sensitive arguments redacted.
- Platform fanout is adapter-based and idempotent around command IDs; offline platform failures produce partial command results without rolling back canonical middleware state.
- The Spring Boot panel under `controlserver/web` is an admin/control surface only; controllers stay thin and route command submissions through `WebControlPanelCommandHandler` into the shared dispatcher.

## Troop Healing
- Troop healing is middleware-first under `middleware/healing`; frontends only consume `TroopHealingProjection` and submit action IDs back to handlers.
- Supported wound types for this pass are `GENERAL_WOUND`, `POISONED`, `MAGIC_WOUND`, and `EXHAUSTED`; burn wounds and modern medical chains are intentionally excluded.
- Food-only fallback healing uses Field Rations and remains available when rations exist, but it is slower than proper treatment and has projection text explaining the tradeoff.
- Proper treatment uses Field Rations plus one of four exposed healing items: Bandage Kit, Antidote Kit, or Arcane Salve where appropriate. Pearl is the general-wound catalyst; Amethyst is the poison and magic-wound catalyst.
- The active gem scope is Pearl, Amethyst, Peridot, Ruby, and Sapphire. Peridot, Ruby, and Sapphire are registered as global resources/domains but are not required by current healing recipes.
- Healing facility definitions cover building levels 1-30 with Field Tent, Infirmary, Herbalist Hut, Apothecary, Field Hospital, Surgical Hall, Shrine, and Guild Hospital bands.
- Resource node philosophy stays older-era/fantasy: wood, food, water, herb, iron, stone, and gem families feed crafting resources; oil and modern industrial inputs are TODO-only for future modernization updates.

## Bot Testing
- Repo-local wrapper scripts in `scripts/` invoke the shared TypeScript smoke harness.
- The bot harness talks QUIC directly so it matches the real client transport.
- QUIC connectivity is handled by the stdio bridge in `tavall-java-game-tools/hytale-bots/scripts/HytaleQuicStdioBridge.java`, which the bot client spawns on demand.
- Dedicated remote wrappers cover castle interaction, resource flow, persistence rehydration, `/kd` alias navigation, data-health status, onboarding UI, UI edge cases, and interior population display updates on `/srv/hytale`.
- Run output is captured in `bot-logs/` so bot failures can be reviewed per run.
