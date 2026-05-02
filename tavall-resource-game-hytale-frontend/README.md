# Hytale Frontend Module

Owns Hytale-specific rendering and input code only.

Current production Hytale plugin classes still live in the root `tavall-hytale-resource-game` module while active UI work is in progress. Move Hytale-only command, UI, and entity-rendering classes here after that work is clean.

This module must delegate gameplay mutations to the middleware/control command pipeline.
