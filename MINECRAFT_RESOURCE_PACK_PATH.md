# Minecraft Resource Pack Path

The Minecraft Bukkit frontend keeps a local resource-pack mount point for castle and building assets and serves a generated `pack.zip` from that mount point when players join.

## Default Path

- Environment variable: `RESOURCE_GAME_MINECRAFT_RESOURCE_PACK_PATH`
- Default value: `resource-pack/`

## Purpose

- Store future custom castle and building assets in one predictable place.
- Generate a zip from that local mount point and force it to clients on join.
- Keep the current playable slice working while the asset tree is still being filled in.
- The plugin bootstraps the local layout on startup and creates `castles/` and `buildings/` folders under the configured root.
- If `distribution/crownbound_minecraft_resource_pack.zip` and its `.sha256.txt` checksum are present under that root, the server validates and serves that imported pack directly.
- The castle and building inventory pages preview files from those folders so the UI reflects the local asset layout directly.
- Castle pages expect filenames like `castle_main.png` and `castle_buildings.json`; building pages expect filenames like `building_detail.png` and `farmstead.json`.

## Current Rule

- The path remains the local source of truth for gameplay assets.
- The server now hosts a generated pack archive over HTTP so clients can be forced to load it.
- Minecraft castles and buildings should remain interactable even before custom assets are added.

## Future Use

- Drop the castle and building model/texture pack contents under this path later.
- Keep the folder local to the Minecraft server runtime; the HTTP pack delivery is only the runtime transport for the local mount point.
