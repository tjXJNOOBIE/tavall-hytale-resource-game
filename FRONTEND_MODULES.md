# Frontend Module Layout

The resource game is split by canonical middleware ownership and platform adapter surface.

The existing `tavall-hytale-resource-game` module remains the current control-server and Hytale plugin implementation while active Hytale UI/runtime work is being migrated. New frontend modules are intentionally thin adapter homes. They must call into the control command/projection pipeline instead of owning canonical gameplay state.

## Modules

| Module | Platform | Language/runtime | Purpose |
| --- | --- | --- | --- |
| `shared-contracts` | Shared | Java | Shared frontend adapter descriptors and future projection/command DTO contracts. |
| `hytale-frontend` | Hytale | Java/Hytale-native | Hytale input, UI, entity rendering, bot-harness-facing hooks. |
| `minecraft-proxy` | Minecraft | Java plugin/native adapter | Minecraft commands, entity/inventory/rendering bridge. |
| `roblox-frontend` | Roblox | Luau | Roblox RemoteEvent, GUI, ProximityPrompt, and asset mapping adapter. |
| `discord-frontend` | Discord | Java bot/buttons | Discord slash commands, buttons, embeds, and account/guild summaries. |
| `android-app` | Android | Kotlin | Future mobile inspection/control shell. |
| `pc-app` | PC app | C# | Future desktop/gameplay shell. |

## Rules

- Canonical account, guild, castle, resource, troop, healing, trade, recon, and market state remains in the Java middleware/control server.
- Frontend modules render projections and submit commands/actions back to middleware.
- Platform-specific IDs stay account bindings only.
- Platform asset references map back to canonical global asset IDs.
- Android and PC app modules are future shells, not canonical state owners.
- Java frontend modules must depend on `shared-contracts` for adapter descriptors instead of redefining platform ownership rules.
- Platform action IDs and interaction-type names belong in the shared action catalog before canonical projection handlers consume them.

## Migration Path

1. Keep current Hytale plugin code running in `tavall-hytale-resource-game`.
2. Keep shared adapter contracts in `shared-contracts`.
3. Move additional platform action catalogs and projection DTO contracts into `shared-contracts`.
4. Move Hytale-only command/UI/rendering classes into `hytale-frontend` once active UI work is clean.
5. Extract middleware/control-server code into a dedicated control module when the frontend module boundaries are stable.
6. Keep Minecraft, Roblox, Discord, Android, and PC app modules consuming shared DTOs/projections only.
