# Asset Pipeline

## Purpose
Keep UI images, font reference sheets, Blockbench source models, and prefab recipes reproducible inside the plugin asset pack.

## Generator
- Run `python scripts/generate-resource-game-assets.py` from the repo root.
- Outputs are deterministic and overwrite only generated asset files.
- Decorative UI assets are mirrored horizontally and vertically so panels, buttons, icons, selectors, and inner ornamentation remain symmetrical.
- Font sheets keep glyphs readable, so symmetry is applied to their cells and frames rather than to each glyph shape.

## UI Texture Outputs
- `src/main/resources/Common/UI/Custom/Textures/ResourceGame/panels/`
- `src/main/resources/Common/UI/Custom/Textures/ResourceGame/buttons/`
- `src/main/resources/Common/UI/Custom/Textures/ResourceGame/icons/`
- `src/main/resources/Common/UI/Custom/Textures/ResourceGame/selectors/`
- `src/main/resources/Common/UI/Custom/Textures/ResourceGame/fonts/`
- `src/main/resources/Common/UI/Custom/Textures/ResourceGame/examples/`
- `src/main/resources/Common/UI/Custom/Textures/ResourceGame/ornaments/`
- `src/main/resources/Common/UI/Custom/Textures/ResourceGame/resource-game-ui-assets.json`

## HyUI Editor Workflow
- Use `https://hytale.ellie.au/` to prototype page overlays, containers, labels, progress bars, text buttons, and back buttons before changing packaged pages.
- Prefer the editor's HYUIML patterns for server pages: `page-overlay`, `container`, `container-title`, `<button>`, `<progress>`, and stable element IDs.
- Keep Resource Game action IDs unchanged when replacing markup, because Java bindings and bot assertions target selectors such as `#EnterInteriorButton`, `#BackButton`, and debug command buttons.
- Copy generated snippets into `src/main/resources/Common/UI/Custom/Pages/*.html`, then keep the generated Resource Game texture references under `Textures/ResourceGame/...`.
- Keep UI pages small and domain-focused so the Minecraft renderer can refresh them without bundling unrelated controls into one screen.
- Runtime button polish is applied by `HyUiPageMarkupDecorator`, which converts page `<button>` elements to raw image-backed Resource Game controls with generated button textures and centered text labels.
- Do not add HyUI `back-button`, `action-button`, `toggle-button`, `item-slot-button`, `native-tab-button`, `custom-button`, or `custom-textbutton` classes to action-bound Resource Game buttons.
- UI clicks should map to internal action names and let the Minecraft-side handlers decide whether the requested screen action is allowed.

## Font References
The font template documents are tracked as PNG sheets and described in `FONT_TEMPLATES.md`.

Current sets:
- Big display alphabet for page titles, banners, and building-name hero text.
- Menu alphabet for standard labels, timers, stats, and action buttons.
- Numbers and symbols for resource counts, costs, timers, coordinates, and debug/status text.

## Blockbench Sources
Blockbench model sources live under `src/main/resources/Common/Models/ResourceGame/`.

Legacy overview models remain available for quick inspection:
- `castle_keep.bbmodel`
- `farmstead.bbmodel`
- `lumber_mill.bbmodel`
- `iron_works.bbmodel`
- `barracks.bbmodel`
- `workshop.bbmodel`

## Imported Blockbench Models
- `src/main/resources/Common/Models/ResourceGame/farmstead.bbmodel` is the farmstead Blockbench source imported from the local Downloads handoff file.
- `src/main/resources/Common/Items/ResourceGame/Farmstead/Model.blockymodel` and `Texture.png` are the native Hytale farmstead model assets imported from `C:\Users\TJ\Documents\HyTaleDevServer\CustomAssets\farmstead`.
- `src/main/resources/Server/Models/ResourceGame/Farmstead.json` registers the farmstead native model asset for runtime entity spawning.
- Farmstead spawn/stage commands resolve the interior farmstead lot through `BuildingPlacementPlanner`; visual refresh uses the packaged native Hytale model asset and does not place the old raw block silhouette.
- `resource_node.bbmodel`

Production-facing generated sources are expanded by gameplay role:
- `castles/castle_keep/level_01.bbmodel` through `level_30.bbmodel`
- `buildings/{building}/level_01.bbmodel` through `level_30.bbmodel` for every `BuildingType`
- `buildings/{building}/construction/{foundation,scaffolding,shell,complete}.bbmodel`
- `nodes/{food,wood,iron}/{full,depleted}.bbmodel`
- `props/{citizen_anchor,troop_anchor,worker_platform,exit_portal,hologram_pedestal,placement_selector_valid,placement_selector_blocked,castle_radius_marker,node_radius_marker}.bbmodel`

`resource-game-model-assets.json` records every generated model and prefab recipe, category counts, and the maximum building level. Tests use this manifest to catch missing Blockbench files before packaging.

## Prefab Recipes
Prefab source recipes live under `src/main/resources/Common/Prefabs/ResourceGame/`.

These recipes intentionally use a repo-local schema, `resource-game-prefab-recipe/v1`, so we can keep source-of-truth block plans versioned before serializing official Hytale `.prefab.json` files.

Run `python scripts/generate-hytale-prefabs.py` to convert farmstead `*.resource-prefab.json` source recipes into Hytale-readable `*.prefab.json` files under `Server/Prefabs`. Pass `--all` when every Resource Game recipe should be converted.

`ResourceGame/farmstead.prefab.json` and `ResourceGame/buildings/farmstead/level_01.prefab.json` are plain generated reference data, not Hytale-native prefab serializer output. `/kd buildings place farmstead` uses the native `Farmstead` model asset instead of these generated JSON prefabs.

Farmstead runtime prefabs:
- `src/main/resources/Server/Prefabs/ResourceGame/farmstead.prefab.json`
- `src/main/resources/Server/Prefabs/ResourceGame/buildings/farmstead/level_01.prefab.json` through `level_30.prefab.json`
- `src/main/resources/Server/Prefabs/ResourceGame/buildings/farmstead/construction/{foundation,scaffolding,shell,complete}.prefab.json`

## Runtime Direction
- Custom UI pages load packaged textures from `Common/UI/Custom/Textures/ResourceGame`.
- Custom in-world models load from the Resource Game asset pack under `Common/Models/ResourceGame`; never copy or generate Resource Game assets into Hytale's default read-only resource pack.
- Building visuals are block-protected gameplay sites plus packaged model entities. Farmstead placement resolves the native Hytale `Farmstead` model asset and does not fall back to the old raw block silhouette.
- `/kd buildings place farmstead` clears the farmstead site, spawns the native Hytale model entity, and intentionally avoids hand-authored `.prefab.json` placement because Hytale prefab files are BSON-style serializer documents, not plain JSON block lists.
- Hot asset sync is only for loose assets copied by `scripts/sync-local-hot-assets.ps1`; do not replace `mods/tavall-hytale-resource-game.jar` while the server is running because the live plugin classloader can read a partially replaced ZIP and fail HyUI page loads with `ZipFile invalid LOC header`.
- Use `scripts/restart-local-dev-server.ps1` for jar updates so the server is stopped first, the jar is copied while offline, and `start.bat` starts the server again.
- Right-clickable Resource Game entities need both data and Java wiring: a root asset under `Server/Item/RootInteractions`, a concrete interaction asset under `Server/Item/Interactions`, an `Interactions` component mapping `InteractionType.Secondary` to the root ID, and a registered Java interaction codec before the asset is decoded.
- The farmstead model and static NPC markers use `OpenFarmstead` as the secondary root interaction. `OpenFarmsteadInteraction` reads `InteractionContext.getEntity()` as the player, `getTargetEntity()` as the clicked entity, and `getCommandBuffer()` for world-thread-safe mutation context before forwarding into existing UI services.
- Runtime prefabs must live under `Server/Prefabs` inside the Resource Game asset pack. `Common/Prefabs` is only for repo-local source recipes and is not used by Hytale's in-game prefab selector/spawner.
- Hytale's current `PrefabListAsset` implementation resolves `RootDirectory: "Asset"` to the default Hytale asset root, not custom asset-pack roots. Do not publish a `Server/PrefabList` entry for generated Resource Game JSON prefabs until an official `/editprefab save` pass creates Hytale-native prefab documents.
- The packaged plugin manifest keeps `IncludesAssetPack: true`, and the local hot asset mirror also sets `IncludesAssetPack: true` so Hytale can index `mods/tavall-hytale-resource-game-hot-assets/Server/Prefabs/ResourceGame`.
- Hologram marker visuals resolve Resource Game pedestal assets before any default Hytale marker, keeping labels tied to the custom pack.
- Hytale prefab placement should use `PrefabStore.getAssetPrefab(...)` or `PrefabStore.getAssetPrefabFromAnyPack(...)`, then place the returned `BlockSelection` on the correct world thread.

See `PREFAB_WORKFLOW.md` for the in-game `/editprefab` and `/prefab` workflow, known jank, and bot-harness verification notes.

## Local Hot Asset Reload
- Run `powershell -ExecutionPolicy Bypass -File scripts/sync-local-hot-assets.ps1 -GenerateAssets -WaitForAssetMonitor` to mirror generated `Common/` and `Server/` assets into `C:\Users\TJ\Documents\HyTaleDevServer\mods\tavall-hytale-resource-game-hot-assets`.
- This creates a folder-based development asset pack with its own `manifest.json`, matching Hytale's documented asset-pack format of a zip or folder with assets in the correct directories.
- The hot asset pack is the only approved local destination for mirrored custom assets; do not write into server default assets or bundled Hytale resource-pack directories.
- Use this for UI textures, Blockbench sources, and prefab recipe iteration. Java code changes still require plugin reload or server restart.

## Hytale Visual Verification
- Run `powershell -ExecutionPolicy Bypass -File scripts/run-hytale-ui-visual-verification.ps1` while the installed Hytale client is open and connected to the local dev server.
- By default, the script writes `visual-control/resource-game-ui-request.properties` into the local server root. The plugin opens the requested UI from the server side, so the Hytale client does not need keyboard focus.
- The script captures before/after screenshots, checks the expected menu region for visual change, checks for Resource Game UI colors, restores the previous foreground window when Hytale steals focus, and writes bounded summaries under `bot-logs/hytale-visual-verification-*`.
- The verification fails when the capture is black/flat, the client is on an auth error window, the server-side control file is not acknowledged, the expected center menu region does not change, or the Resource Game UI overlay colors are not visible after opening the menu.
