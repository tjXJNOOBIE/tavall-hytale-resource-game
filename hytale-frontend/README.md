# Hytale Frontend Module

Owns Hytale-specific rendering and input code only.

Current production Hytale plugin classes still live in the root `tavall-hytale-resource-game` module while active UI work is in progress. Move Hytale-only command, UI, and entity-rendering classes here after that work is clean.

This module must delegate gameplay mutations to the middleware/control command pipeline.

## Control ingress and DI

| Component | Responsibility |
|---|---|
| `HytaleFrontendDependencyModule` | Registers the single-surface Hytale adapter behind Tavall DI interface tokens |
| `IHytaleFrontendDomainGenerated` | Provides default accessors for config, command envelopes, KD formatting, control client, and bridge |
| `HytaleControlPlaneCommandBridge` | Translates Hytale command/action input into backend control-ingress requests |
| `FrontendTcpControlCommandClient` | Posts `HYTALE` envelopes to the configured control bridge socket |
| `FrontendCommandVerificationHandler` | Core plugin adapter that calls the Hytale domain default methods instead of owning a constructor-wired bridge |

Hytale remains a single-surface frontend. It does not need a Velocity-style proxy split, and it must not mutate canonical gameplay state directly from UI/input events.
