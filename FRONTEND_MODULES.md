# Frontend Module Layout

The resource game is split by canonical middleware ownership and platform adapter surface.

On `minecraft-main`, the branch carries the canonical backend plus the Minecraft-facing adapters that need to ship together. Platform-specific modules for Hytale, Roblox, and the PC app live on their own platform branches instead of staying in this tree.

## Modules

| Module | Platform | Language/runtime | Purpose |
| --- | --- | --- | --- |
| `game-api` | Shared | Java | Shared frontend adapter descriptors and future projection/command DTO contracts. |
| `minecraft-proxy` | Minecraft | Java plugin/native adapter | Minecraft commands, entity/inventory/rendering bridge. |
| `discord-frontend` | Discord | Java bot/buttons | Discord slash commands, buttons, embeds, and account/guild summaries. |
| `android-app` | Android | Kotlin | Future mobile inspection/control shell. |
| `control-server` | Backend/control plane | Java | Canonical gameplay state, cloud/control console, panel, transport, persistence, and control APIs. |

## Rules

- Canonical account, guild, castle, resource, troop, healing, trade, recon, and market state remains in the Java middleware/control server.
- Frontend modules render projections and submit commands/actions back to middleware.
- Platform-specific IDs stay account bindings only.
- Platform asset references map back to canonical global asset IDs.
- Android is a future shell, not a canonical state owner.
- Java frontend modules must depend on `game-api` for adapter descriptors instead of redefining platform ownership rules.
- Platform action IDs and interaction-type names belong in the shared action catalog before canonical projection handlers consume them.

## Migration Path

1. Keep shared adapter contracts in `game-api`.
2. Keep canonical gameplay/control logic in `control-server`.
3. Keep Minecraft adapters thin: `minecraft-proxy` and `minecraft-game-server` should render or route, not own canonical state.
4. Move additional platform action catalogs and projection DTO contracts into `game-api`.
5. Add other platform modules back only on their own branches when those adapters are actively being developed.
