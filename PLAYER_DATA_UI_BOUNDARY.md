# Player Data UI Boundary

This repo keeps Minecraft UI reads narrow:

- Minecraft UI code requests player data from the control plane.
- The control plane owns the canonical player data and assembles the view model.
- Minecraft renders the UI locally from that returned data.
- Minecraft does not read canonical player data directly from control-plane repositories.

## Current Scope

For now, this pass only exposes player data for Minecraft UI display.
That includes the account/profile information needed by the existing kingdom account screen.

The control-plane entry point is the `api` package:

- `org.tavall.control.api.PlayerDataApi`

The shared transport contracts live in the shared frontend contract package:

- `PlayerDataRequest`
- `PlayerDataResponse`
- `PlayerPlatformBindingView`

Minecraft uses the typed bridge path to fetch that data and then converts the response into the existing GUI model.

## Flow

1. Player opens a Minecraft UI that needs account data, usually through `/kd account`, `/kd data status`, or `/kd ui account`.
2. Minecraft sends a typed player-data request to the control plane.
3. The control plane reads the canonical account data and any binding data it needs.
4. The control plane returns a `PlayerDataResponse`.
5. Minecraft renders the UI from that response.

## Rules

- Keep UI reads on the control-plane API boundary.
- Do not add direct repository access from Minecraft for canonical player data.
- Do not expand this pass into general backend gameplay surfaces.
- If new Minecraft UIs need player data later, route them through the same control-plane API pattern.

## Related Implementation

- Minecraft UI renderer: `KingdomAccountGui`
- Minecraft command client: `MinecraftBukkitCommandClientHandler`
- Control-plane bridge server: `ControlPlaneTcpBridgeServer`
- Control-plane player API: `PlayerDataApi`

## Follow-up

Future gameplay surfaces can use the same typed bridge pattern, but they should be added separately and intentionally.
