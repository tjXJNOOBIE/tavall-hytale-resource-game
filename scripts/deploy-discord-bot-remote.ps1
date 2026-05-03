param(
    [string]$HostName = "146.235.232.128",
    [string]$User = "ubuntu",
    [string]$KeyPath = "C:\Users\TJ\Documents\.ssh\NovusKey.key",
    [string]$RemoteDirectory = "/opt/tavall-resource-game",
    [int]$ControlServerPort = 8080
)

$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$discordJar = Join-Path $root "tavall-resource-game-discord-frontend\target\tavall-resource-game-discord-frontend-0.1.0-SNAPSHOT.jar"
$controlServerJar = Join-Path $root "target\tavall-hytale-resource-game.jar"

if (!(Test-Path $discordJar)) {
    throw "Discord bot jar not found: $discordJar"
}
if (!(Test-Path $controlServerJar)) {
    throw "Control server jar not found: $controlServerJar"
}

$target = "$User@$HostName"
$remoteDiscordTmp = "/tmp/resource-game-discord-bot.jar"
$remoteControlTmp = "/tmp/resource-game-control-server.jar"

scp -i $KeyPath $discordJar "${target}:$remoteDiscordTmp"
scp -i $KeyPath $controlServerJar "${target}:$remoteControlTmp"

$remoteScript = @"
set -euo pipefail
sudo mkdir -p '$RemoteDirectory'
sudo mv '$remoteDiscordTmp' '$RemoteDirectory/resource-game-discord-bot.jar'
sudo mv '$remoteControlTmp' '$RemoteDirectory/resource-game-control-server.jar'
sudo chown -R ubuntu:ubuntu '$RemoteDirectory'
if [ ! -f '$RemoteDirectory/resource-game-discord.env' ]; then
  cat > '$RemoteDirectory/resource-game-discord.env' <<'ENVEOF'
RESOURCE_GAME_DISCORD_BOT_TOKEN=
RESOURCE_GAME_DISCORD_GUILD_ID=
RESOURCE_GAME_CONTROL_INGRESS_URL=http://127.0.0.1:$ControlServerPort/api/frontend/commands
RESOURCE_GAME_DISCORD_OWNER_USER_IDS=
RESOURCE_GAME_DISCORD_ADMIN_ROLE_IDS=
RESOURCE_GAME_DISCORD_ADMIN_ROLE_NAMES=
RESOURCE_GAME_DISCORD_MODERATOR_ROLE_IDS=
RESOURCE_GAME_DISCORD_MODERATOR_ROLE_NAMES=
ENVEOF
  chmod 600 '$RemoteDirectory/resource-game-discord.env'
fi
sudo tee /etc/systemd/system/resource-game-web-panel.service >/dev/null <<'UNITEOF'
[Unit]
Description=Tavall Resource Game Optional Web Control Panel
After=network-online.target
Wants=network-online.target

[Service]
User=ubuntu
WorkingDirectory=$RemoteDirectory
ExecStart=/usr/bin/java -cp $RemoteDirectory/resource-game-control-server.jar com.tavall.hytale.resourcegame.controlserver.web.ControlServerApplication --server.port=$ControlServerPort
Restart=on-failure
RestartSec=5

[Install]
WantedBy=multi-user.target
UNITEOF
sudo tee /etc/systemd/system/resource-game-discord-bot.service >/dev/null <<'UNITEOF'
[Unit]
Description=Tavall Resource Game Discord Bot
After=network-online.target resource-game-web-panel.service
Wants=network-online.target

[Service]
User=ubuntu
WorkingDirectory=$RemoteDirectory
EnvironmentFile=$RemoteDirectory/resource-game-discord.env
ExecStart=/usr/bin/java -jar $RemoteDirectory/resource-game-discord-bot.jar
Restart=on-failure
RestartSec=5

[Install]
WantedBy=multi-user.target
UNITEOF
sudo systemctl daemon-reload
if command -v ufw >/dev/null 2>&1 && sudo ufw status | grep -q 'Status: active'; then
  sudo ufw allow $ControlServerPort/tcp
fi
echo 'Deployed jars and systemd units.'
echo 'Fill resource-game-discord.env, then start resource-game-web-panel and resource-game-discord-bot.'
"@

ssh -i $KeyPath -o StrictHostKeyChecking=no -o BatchMode=yes $target $remoteScript
