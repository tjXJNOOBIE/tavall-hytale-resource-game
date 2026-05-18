# UI System

## Purpose
Keep UI content data-driven. Control-server owns the read models that fill screens, while the Minecraft runtime owns presentation, input handling, and refresh behavior.

## Responsibilities
- Expose UI data through `UIData` and domain-specific getters.
- Keep reusable screen and action contracts in `minecraft-framework`.
- Let `minecraft-game-server` own page rendering, inventory layout, and click handling.
- Keep navigation and page selection logic out of the control plane.

## Main classes
- `UIData`
- `UiAction`
- `UiScreen`
- `UiSection`
- `UiScreenKey`
- Minecraft-side UI handlers and page renderers

## Important boundary
- `UIData` is read-only state and should only pull from the cache, database, or both as needed.
- `minecraft-framework` owns reusable UI contracts, not gameplay state.
- `minecraft-game-server` decides how those contracts render in Minecraft and how clicks map back into gameplay or control-plane actions.
- The old `UiNavigator`, `UiPageRegistry`, `UiActionService`, and `UiPageType` surfaces are intentionally gone.

## Links to other systems
- Castle, interior, resource, troop, player, and node systems provide the domain data shown by the UI.
- Interaction systems decide which data set a player should see.
- Control-plane commands and caches provide the backing read models.
- Art direction for panels, icons, buttons, placement selectors, and interaction states lives in [ART_DIRECTION.md](./ART_DIRECTION.md).

## Notes
- Keep UI models narrow and domain-owned.
- Prefer explicit screen keys and action contracts over generic navigation helpers.
- Join-time auto-open remains opt-in so players can enter the world normally.
