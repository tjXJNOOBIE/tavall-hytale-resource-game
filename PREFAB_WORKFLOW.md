# Hytale Prefab Workflow

## In-Game Authoring Flow
- Create a prefab editing world with `/editprefab new my_prefab_world`.
- Build the structure inside that editing world and keep entities light unless the prefab explicitly needs them.
- Use the selection brush to capture the full bounding box, including roofs, props, ground trim, and any vertical clearance.
- Save the selected structure with `/prefab save my_prefab_name`.
- Exit the editing world with `/editprefab exit`.
- Place the saved prefab through the Paste brush server dropdown, or use `/prefab load my_prefab_name`.

## Useful Commands
- `/prefab list` lists saved prefab files available to the server.
- `/prefab load my_prefab_name` selects or loads a prefab for placement.
- `/prefab delete my_prefab_name` deletes a prefab file, though some builds can throw thread assertions here.
- `/prefab info my_prefab_name` shows prefab metadata.
- `/editprefab load my_prefab_name` opens an existing prefab in an edit session.
- `/editprefab save` writes edits back to the loaded prefab.
- `/editprefab saveas my_prefab_variant` writes edits as a new prefab.
- `/editprefab setbox`, `/editprefab info`, `/editprefab tp`, and `/editprefab modified` help inspect or adjust the edit session.

## Resource Game Conventions
- Prefab files are build assets; plugin code is gameplay logic. Keep structure data out of Java except for defensive fallbacks on non-farmstead buildings.
- Runtime Hytale prefabs live under `src/main/resources/Server/Prefabs/ResourceGame/`.
- Hytale's current `PrefabListAsset` root named `Asset` resolves to the default Hytale asset root, not to custom asset-pack roots. Generated Resource Game JSON prefabs should not be listed there until they are converted through an official `/editprefab save` workflow.
- Hytale-native `.prefab.json` files are not normal text JSON. They are BSON-style documents written by Hytale's prefab serializer. Do not hand-author plain JSON files with `version`, `anchorX`, and `blocks` and expect `/prefab load` or the Paste brush to use them; Hytale can reject or replace them with normal fallback blocks.
- Hytale-native model spawning should use `Server/Models/*.json` plus `Common/**/*.blockymodel` and texture assets. Model asset references are validated against allowed Common roots such as `Items/`, `NPC/`, `Characters/`, and `VFX/`. Farmstead uses `Server/Models/ResourceGame/Farmstead.json`, `Common/Items/ResourceGame/Farmstead/Model.blockymodel`, and `Common/Items/ResourceGame/Farmstead/Texture.png`.
- Do not expect a model entity to become right-clickable from `ModelComponent` alone. Add an `Interactions` component with `InteractionType.Secondary`, point it at a root interaction JSON, and register the Java interaction codec before asset load. Farmstead uses `Server/Item/RootInteractions/OpenFarmstead.json` and `Server/Item/Interactions/OpenFarmsteadInteraction.json`.
- Source recipes live under `src/main/resources/Common/Prefabs/ResourceGame/` and are converted with `python scripts/generate-hytale-prefabs.py`.
- The farmstead command path uses the native Hytale model asset. Plain generated prefab JSON is kept as source/reference data only and is not used for farmstead placement.
- Gameplay references should use stable prefab IDs such as `ResourceGame/farmstead.prefab.json`, `ResourceGame/buildings/farmstead/level_01.prefab.json`, and `ResourceGame/buildings/farmstead/construction/foundation.prefab.json`.
- Custom assets must be mirrored into `C:\Users\TJ\Documents\HyTaleDevServer\mods\tavall-hytale-resource-game-hot-assets`; do not write custom files into Hytale's default read-only resource pack.

## Local Iteration
- Run `python scripts/generate-hytale-prefabs.py` after changing prefab recipes or the farmstead Blockbench source.
- Run `powershell -ExecutionPolicy Bypass -File scripts/sync-local-hot-assets.ps1 -ProcessResources -WaitForAssetMonitor -WaitSeconds 5` to mirror hot assets.
- Restart Java plugin changes through `C:\Users\TJ\Documents\HyTaleDevServer\start.bat`; do not bypass that script.
- If a saved edit or pasted preview looks stale, exit and re-enter the world, then toggle the Paste brush preview material/view with `T`.

## Bot Harness Notes
- Focused harness checks should open the local server, run `/prefab list`, then run `/kd buildings place farmstead`.
- The expected farmstead result is the native Hytale farmstead model asset, not the old raw Java block silhouette and not a hand-authored JSON prefab.
- If no Hytale player is online, the visual control scripts cannot verify placement; keep the asset validation and focused Java tests as the fallback signal.
