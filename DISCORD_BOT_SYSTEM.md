# Discord Bot System

The Discord frontend is an adapter for the resource game control server. Discord slash commands never own canonical gameplay state.

Command flow:

1. `/kingdom <command>` or `/kingdom-verify` runs as the user command lane.
2. `/kingdom-admin <command>` runs as the admin command lane.
3. The Discord bot resolves the Discord user, guild owner state, and configured role mappings into a shared universal permission subject.
4. Allowed commands become a `FrontendCommandEnvelope`.
5. The bot submits the envelope through the typed TCP control bridge.
6. The control server runs `FrontendCommandIngressHandler`, command translation, audit logging, fanout, and projection refresh.

Required environment variables:

- `RESOURCE_GAME_DISCORD_BOT_TOKEN`: Discord bot token.
- `RESOURCE_GAME_DISCORD_GUILD_ID`: Project Novus Discord guild id for guild-scoped command registration.
- `RESOURCE_GAME_CONTROL_INGRESS_URL`: control bridge URL. Default is `tcp://127.0.0.1:18081`.

Permission mapping variables:

- `RESOURCE_GAME_DISCORD_OWNER_USER_IDS`: comma-separated Discord user ids with owner access.
- `RESOURCE_GAME_DISCORD_ADMIN_ROLE_IDS`: comma-separated Discord role ids that can run `/kingdom-admin`.
- `RESOURCE_GAME_DISCORD_ADMIN_ROLE_NAMES`: comma-separated Discord role names that can run `/kingdom-admin`.
- `RESOURCE_GAME_DISCORD_MODERATOR_ROLE_IDS`: comma-separated Discord role ids with moderator access.
- `RESOURCE_GAME_DISCORD_MODERATOR_ROLE_NAMES`: comma-separated Discord role names with moderator access.

Commands registered by the bot:

- `/kingdom command:<text>`: user lane command routed through the control server.
- `/kingdom-admin command:<text>`: admin lane command routed through the control server.
- `/kingdom-verify`: live verification ping routed through the control server.

Remote deployment:

1. Package the bot with `mvn -f discord-frontend/pom.xml -DskipTests package`.
2. Package the control server with `mvn -f pom.xml -DskipTests package`.
3. Run `scripts/deploy-discord-bot-remote.ps1`.
4. Fill `/opt/tavall-resource-game/resource-game-discord.env` on the remote server with the bot token, guild id, and role mappings.
5. Start the optional web/control HTTP surface with `control start web-panel` locally, or `sudo systemctl start resource-game-web-panel` on the remote deployment.
6. Start or restart `resource-game-discord-bot` on the remote server after the env file is populated.

Discord installation note:

The bot cannot approve its own Discord guild install, but the repo can open the OAuth install URL for the logged-in local Discord session:

```powershell
$env:RESOURCE_GAME_DISCORD_CLIENT_ID = "<discord application id>"
$env:RESOURCE_GAME_DISCORD_GUILD_ID = "<project novus guild id>"
.\scripts\open-discord-bot-install.ps1
```

If `RESOURCE_GAME_DISCORD_CLIENT_ID` is not set, the script can discover it from `RESOURCE_GAME_DISCORD_BOT_TOKEN`. After the bot is present in the guild, it registers the slash commands automatically using `RESOURCE_GAME_DISCORD_GUILD_ID`.

Ports:

- The Discord bot uses outbound HTTPS and does not require inbound ports.
- The optional Spring web panel/control HTTP ingress uses port `8080` by default when launched with `control start web-panel`.
- If a reverse proxy is used, expose only the proxy port publicly and keep the control-server ingress bound internally.

Verification:

- Local HTTP ingress verification: `scripts/run-discord-control-live-check.ps1`.
- Discord live verification after install: run `/kingdom-verify` in Project Novus and confirm the reply reports `LOCAL_ACTION_ALLOWED` or `DISPATCHED` with success `true`.
- Admin/user split verification: a normal member should be able to run `/kingdom command:ui discord-live-verify` and should be rejected by `/kingdom-admin`; an owner/admin role should be allowed on `/kingdom-admin`.

TODO:

- Add persistent remote secret management for the Discord token instead of editing the environment file manually.
- Add HTTPS/reverse-proxy hardening for the optional remote web panel/control ingress.
- Add Discord button/select-menu follow-up actions for projected summaries.
- Add universal permission persistence so Discord role-derived grants and MCRSpeedrun rank-derived grants share the same grant repository.
