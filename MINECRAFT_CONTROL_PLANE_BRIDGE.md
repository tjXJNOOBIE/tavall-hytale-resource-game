# Minecraft / Control Plane Bridge

The Minecraft frontends now talk to the standalone control-plane console over a local typed TCP bridge.

Policy rule:

- Do not use HTTP for our own private control-plane or gameplay data paths.
- Prefer direct Java objects inside a process, or the typed TCP bridge between trusted processes.
- HTTP is reserved for public APIs and external-facing integrations, which are not part of this repo's current scope.

## Runtime shape

- Control plane entrypoint: `org.tavall.control.cli.ControlConsoleApplication`
- Control bridge server: `org.tavall.control.transport.ControlPlaneTcpBridgeServer`
- Control bridge client: `org.tavall.control.transport.ControlPlaneTcpBridgeClient`
- Minecraft client config: `org.tavall.control.services.FrontendControlConfig`
- Minecraft command client: `org.tavall.control.services.FrontendTcpControlCommandClient`

Default bridge address:

- host: `127.0.0.1`
- port: `18081`

Supported overrides:

- `TAVALL_CONTROL_BRIDGE_HOST`
- `TAVALL_CONTROL_BRIDGE_PORT`
- `RESOURCE_GAME_CONTROL_BRIDGE_HOST`
- `RESOURCE_GAME_CONTROL_BRIDGE_PORT`
- `RESOURCE_GAME_MINECRAFT_CONTROL_BRIDGE_URL`
- `RESOURCE_GAME_CONTROL_BRIDGE_URL`

## Wire format

The bridge is line-delimited JSON.

- Minecraft sends a typed `FrontendCommandEnvelope`
- Control plane returns `FrontendTcpControlBridgeResponse`
- The response contains either a `FrontendCommandVerificationResult`, `InteractionResult`, `PlayerDataResponse`, `RankResponse`, or an error message

## Command flow

1. A Minecraft `/kd ...` or `/kingdom ...` command is submitted from the Bukkit frontend.
2. `FrontendCommandIngressHandler` translates the surface command into control-plane input.
3. The Minecraft frontend sends the typed envelope to the control bridge.
4. The standalone control plane validates and executes the command.
5. The response is routed back to Minecraft and shown to the player.

Rank flow:

1. Velocity builds a typed `RankRequest` from the player or console source.
2. The proxy sends the request to the control-plane `RankApi`.
3. The control plane reads or updates the canonical operator repository.
4. Velocity renders the returned `RankResponse` directly in chat.

## Interaction flow

1. The Bukkit frontend resolves a tagged NPC or building marker.
2. Minecraft opens the existing inventory GUI for the resolved target.
3. GUI clicks are sent back through the same typed command bridge.
4. The control plane validates the action before any canonical state change is applied.

## Notes

- Minecraft should not instantiate the control plane runtime directly.
- Control-plane state lives in the plain Java process, not in Bukkit adapters.
- Velocity should not mutate rank state locally; it only fronts the control-plane API.
- If the bridge is offline, the player gets a rejected result instead of a crash.

## Start order

1. Start the standalone control plane with `scripts/dev/start-control-plane.sh`.
2. Start the Minecraft server or the full dev stack.
3. Attach to the `tmux` session and use the Minecraft commands or GUI.
