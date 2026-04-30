# Postgres Schema

Apply files in order:
1. 001_player_profile.sql
2. 002_player_game_state.sql
3. 003_player_game_state_evolution.sql
4. 004_cross_platform_middleware.sql

The first three files are the existing Hytale player profile/game-state tables. `004_cross_platform_middleware.sql`
adds canonical cross-platform middleware tables for universal identity, auth/platform bindings, 2FA,
global assets, guilds, castles, resource nodes, taxes, petitions, propaganda, troops, troop wounds,
healing inventory, healing facilities, healing plans, trade routes, control operators, control command
results, command audit logs, platform fanout retry records, and scheduled control commands.

The evolution script is intentionally idempotent. It keeps existing local or remote development
databases compatible as gameplay fields move from placeholder metadata into first-class columns.
