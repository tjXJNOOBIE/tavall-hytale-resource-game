# Minecraft Frontend Module

Owns Minecraft-specific commands, input bridges, resource-pack references, entity/inventory debug output, and server adapter hooks.

Canonical gameplay state must stay in the middleware/control server. Minecraft commands should parse platform input and dispatch canonical control commands.
