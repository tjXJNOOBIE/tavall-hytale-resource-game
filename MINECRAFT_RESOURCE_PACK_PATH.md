# Minecraft Resource Pack Path

The Minecraft Bukkit frontend keeps a local resource-pack mount point for future castle and building assets.

## Default Path

- Environment variable: `RESOURCE_GAME_MINECRAFT_RESOURCE_PACK_PATH`
- Default value: `resource-pack/`

## Purpose

- Store future custom castle and building assets in one predictable place.
- Keep the current playable slice working with vanilla placeholders until assets exist.
- Avoid HTTP-based private asset delivery.
- The plugin bootstraps the local layout on startup and creates `castles/` and `buildings/` folders under the configured root.
- The castle and building inventory pages preview files from those folders so the UI reflects the local asset layout directly.
- Castle pages expect filenames like `castle_main.png` and `castle_buildings.json`; building pages expect filenames like `building_detail.png` and `farmstead.json`.

## Current Rule

- The path is a local filesystem mount point only.
- We do not require a generated pack or remote asset server for the current pass.
- Minecraft castles and buildings should remain interactable even before custom assets are added.

## Future Use

- Drop the castle and building model/texture pack contents under this path later.
- Wire any eventual pack build or distribution step from this path, not from the gameplay command path.
- Keep the folder local to the Minecraft server runtime; do not add HTTP delivery for private assets.
