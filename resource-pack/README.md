# Resource Pack Root

This folder is the local mount point for Tavall castle and building assets. The Minecraft server now zips the files in this tree into `pack.zip` and forces that archive to clients on join.

## Expected Layout

- `castles/`
- `buildings/`
- `distribution/`

The Minecraft server bootstrap creates these folders automatically when it starts. Keep private gameplay assets here; the server runtime handles pack delivery for you.

## Bundled Pack

When `distribution/crownbound_minecraft_resource_pack.zip`, `distribution/crownbound_minecraft_resource_pack.sha256.txt`, and `distribution/crownbound_minecraft_resource_pack.sha1.txt` exist, the server prefers that imported pack over generating a pack from the loose local asset tree.

The castle and building inventory screens also preview file names from these folders so artists and devs can see which local assets are available.

Expected examples:
- `castles/castle_main.png`
- `castles/castle_buildings.json`
- `buildings/building_detail.png`
- `buildings/farmstead.json`
