# Test System

## Purpose
Cover gameplay and infrastructure behavior with both in-memory Java tests and remote bot scenarios that exercise the real Hytale runtime.

## Layers
- Java tests: fast, deterministic, in-memory coverage for planners, metadata, persistence behavior, and service orchestration.
- Remote QUIC bot flows: integration coverage against the running remote Hytale server.
- Local deployment verification: rebuild plugin jar, deploy only the plugin jar, restart dev server, and verify server boot.
- Remote Hytale bot flows now start from the exact local `C:\Users\TJ\Documents\HytaleDevServer` tree. `sync-remote-hytale-dev-server.ps1` archives that folder, copies it to `/srv/hytale/HytaleDevServer`, then adds remote-only Linux shims so existing runners can start it and resolve `Server/...` paths against the copied folder.
- Remote Minecraft proxy flows deploy the resource-game control bridge to `/srv/resource-game-control` on port `18081`, deploy the shaded Velocity plugin to `/srv/proxy/plugins`, ensure the configured backend `/srv/ffa` is listening on `25566`, bring up a second switch backend on `25567`, map `kingdom-2-minecraft-primary` to Velocity server `ffa`, and run Mineflayer through the real remote Velocity proxy on `25565`.

## Main Java coverage areas
- Cache round-trip behavior
- Profile/game-state load-or-create
- Metadata hydration and onboarding flags
- Economy tick behavior
- Node stock depletion/regeneration
- Castle placement persistence and visual refresh fan-out
- Scene planner scaling and readability math

## Main bot flow areas
- Connect-only and world join
- Castle near/look interaction
- Focus-and-interact world targeting
- Resource-game UI flow
- Interior cycle and tour
- Persistence and rehydration
- Control-plane clock/schedule/aging commands through the real Hytale player bot (`remote-control-plane-clock-flow.mjs`)
- Control-plane kingdom/coordinate/instance/parameter/clock/citizen commands through the real Minecraft Velocity proxy (`minecraft-velocity-control-plane-flow.mjs`)
- Placement flow
- Node assignment flow
- UI edge behavior
- Visual counter flow

## Log handling
- Normal runs keep compact artifacts in `bot-logs/`.
- Heavy transcripts are minimized unless explicitly requested.
- `prune-bot-logs.ps1` removes stale artifacts so bot logs do not grow without bound.
- `run-remote-full-suite.ps1` now retries each remote step once before failing the aggregate suite, because the QUIC harness still has intermittent disconnects that do not reflect plugin regressions.
- `run-remote-full-suite.ps1` syncs the exact Documents Hytale dev server folder before installing HyUI, syncing the bot harness, or running scenarios. The remote bot harness still lives alongside the copied server at `/srv/hytale/_bot/hytale-sim`.
- `run-remote-control-plane-clock-flow.ps1` also syncs `C:\Users\TJ\Documents\HytaleDevServer` by default before starting the remote server, then runs the player-like bot beside it and sends `/kd clock`, `/kd schedule`, and `/kd aging` chat commands that the server reads.
- `run-remote-minecraft-velocity-control-plane-flow.ps1` packages the live path around the remote proxy. It restarts Velocity with `RESOURCE_GAME_MINECRAFT_CONTROL_INGRESS_URL`, `RESOURCE_GAME_MINECRAFT_INSTANCE_SERVER_MAP`, and the configured bot username as a Minecraft control-plane owner, then stores result/transcript artifacts in `bot-logs/`.

## Known limitations
- Shared bot client support for native world-click packets is still incomplete for this repo's needs.
- Placement scenarios currently use align -> look -> focus/interact or aim-confirm to stay close to player behavior without patching over dirty shared bot code.
- Restart-sensitive remote suites wait for an explicit remote boot marker before they run, not just an open socket.
- Some remote flows intentionally use admin commands after a real UI/world open so the scenario can validate gameplay state deterministically even when the shared bot client does not fire reliable CustomUI button events.
