# TODO - First Joiners

## Placement Flow
- Replace the current spawn-at-player-location castle placement with a guided placement flow.
- Add a first-join UI walkthrough explaining castle ownership and location confirmation.
- Add cancellation/redo flow for castle placement.
- Middleware placement bridge remains: `CastleCreationHandler` and `CastlePlacementHandler` now store canonical castle state by `UniversalPlayerId`, but the live first-join Hytale flow still uses existing session/player UUID placement; this remains because active command/UI files have unrelated uncommitted work and should be bridged deliberately through `KingdomPlacementCommandSupport`, `PlayerDataService`, and `HytaleProjectionHandler`.
