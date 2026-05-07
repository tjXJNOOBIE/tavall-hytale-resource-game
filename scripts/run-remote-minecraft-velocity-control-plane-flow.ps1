param(
    [string]$SshAlias = "novus-remote",
    [string]$SshConfigPath = "C:\Users\TJ\.ssh\config",
    [string]$RemoteProxyDir = "/srv/proxy",
    [string]$RemoteBackendDir = "/srv/ffa",
    [string]$RemoteSwitchBackendDir = "/srv/ffa-switch",
    [int]$SwitchBackendPort = 25567,
    [string]$RemoteKingdomServersDir = "/srv/mc-kingdom-servers",
    [string]$RemoteKingdomBackendName = "mc-kingdom-server-1",
    [int]$KingdomBackendPort = 25568,
    [string]$KingdomVelocityServerName = "kingdom",
    [string]$RemoteControlDir = "/srv/resource-game-control",
    [string]$RemoteHeadlessDir = "/srv/headless",
    [string]$ControlServerJarPath = "F:/workspace/TavallMonoRepo/tavall-java-hytale-games/tavall-hytale-resource-game/target/tavall-hytale-resource-game.jar",
    [string]$PluginJarPath = "F:/workspace/TavallMonoRepo/tavall-java-hytale-games/tavall-hytale-resource-game/tavall-resource-game-minecraft-frontend/target/tavall-resource-game-minecraft-frontend-0.1.0-SNAPSHOT.jar",
    [string]$ServerPluginJarPath = "F:/workspace/TavallMonoRepo/tavall-java-hytale-games/tavall-hytale-resource-game/tavall-resource-game-minecraft-server-frontend/target/tavall-resource-game-minecraft-server-frontend-0.1.0-SNAPSHOT.jar",
    [string]$ScenarioScriptPath = "F:/workspace/TavallMonoRepo/tavall-java-hytale-games/tavall-hytale-resource-game/scripts/minecraft-velocity-control-plane-flow.mjs",
    [int]$ControlPort = 18080,
    [string]$ControlIngressUrl = "http://127.0.0.1:18080/api/frontend/commands",
    [string]$BotUsername = "ResourceProxyBot",
    [string]$MinecraftVersion = "1.8.9",
    [string]$InstanceServerMap = "kingdom-1-minecraft-primary=kingdom,kingdom-2-minecraft-primary=ffa",
    [string]$CommandList = "/server kingdom|/tavallserver snapshot|/kd clock state kingdom-1|/kingdom citizens summary kingdom-1",
    [string]$LogDir = "bot-logs"
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
$logRoot = Join-Path $repoRoot $LogDir
New-Item -ItemType Directory -Force -Path $logRoot | Out-Null

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$baseName = "remote-minecraft-velocity-control-plane-$timestamp"
$runLogPath = Join-Path $logRoot "$baseName-run.txt"
$resultPath = Join-Path $logRoot "$baseName-result.json"
$transcriptPath = Join-Path $logRoot "$baseName-transcript.txt"
$remotePluginPath = "$RemoteProxyDir/plugins/tavall-resource-game-minecraft-frontend.jar"
$remoteBackendPluginPath = "$RemoteBackendDir/plugins/tavall-resource-game-minecraft-server-frontend.jar"
$remoteSwitchBackendPluginPath = "$RemoteSwitchBackendDir/plugins/tavall-resource-game-minecraft-server-frontend.jar"
$RemoteKingdomBackendDir = "$RemoteKingdomServersDir/$RemoteKingdomBackendName"
$remoteKingdomBackendPluginPath = "$RemoteKingdomBackendDir/plugins/tavall-resource-game-minecraft-server-frontend.jar"
$remoteScriptPath = "$RemoteHeadlessDir/$baseName.mjs"
$remoteOutputDir = "/tmp/$baseName"

function Write-LogLine {
    param([string]$Message)
    Add-Content -Path $runLogPath -Value $Message -Encoding utf8
    Write-Host $Message
}

function Invoke-Checked {
    param([string]$FilePath, [string[]]$Arguments, [string]$FailureMessage)
    & $FilePath @Arguments 2>&1 | Tee-Object -FilePath $runLogPath -Append
    if ($LASTEXITCODE -ne 0) {
        throw $FailureMessage
    }
}

if (-not (Test-Path $PluginJarPath)) {
    throw "Minecraft Velocity plugin jar not found at $PluginJarPath"
}
if (-not (Test-Path $ServerPluginJarPath)) {
    throw "Minecraft Bukkit server plugin jar not found at $ServerPluginJarPath"
}
if (-not (Test-Path $ControlServerJarPath)) {
    throw "Control server jar not found at $ControlServerJarPath"
}
if (-not (Test-Path $ScenarioScriptPath)) {
    throw "Scenario script not found at $ScenarioScriptPath"
}

Write-LogLine "[$((Get-Date).ToString("o"))] Deploying resource-game control ingress."
Invoke-Checked -FilePath "ssh.exe" -Arguments @("-F", $SshConfigPath, $SshAlias, "mkdir -p '$RemoteControlDir/logs'") -FailureMessage "Failed to prepare remote control directory."
Invoke-Checked -FilePath "scp.exe" -Arguments @("-F", $SshConfigPath, $ControlServerJarPath, "$SshAlias`:$RemoteControlDir/tavall-hytale-resource-game.jar.new") -FailureMessage "Failed to copy control server jar."
$remoteControl = @"
set -euo pipefail
cd '$RemoteControlDir'
mv tavall-hytale-resource-game.jar.new tavall-hytale-resource-game.jar
CONTROL_PIDS=`$(pgrep -f '^java .*ControlServerApplication' || true)
if [ -n "`$CONTROL_PIDS" ]; then
  echo "`$CONTROL_PIDS" | xargs -r kill || true
fi
for i in `$(seq 1 30); do
  if ! ss -ltn | grep -q ':$ControlPort '; then
    break
  fi
  sleep 1
done
if ss -ltn | grep -q ':$ControlPort '; then
  CONTROL_PIDS=`$(pgrep -f '^java .*ControlServerApplication' || true)
  if [ -n "`$CONTROL_PIDS" ]; then
    echo "`$CONTROL_PIDS" | xargs -r kill -9 || true
  fi
fi
for i in `$(seq 1 10); do
  if ! ss -ltn | grep -q ':$ControlPort '; then
    break
  fi
  sleep 1
done
if ss -ltn | grep -q ':$ControlPort '; then
  echo 'Resource-game control ingress port is still occupied after shutdown.' >&2
  ss -ltnp | grep ':$ControlPort ' >&2 || true
  exit 1
fi
nohup java --enable-preview -Dserver.port=$ControlPort -cp tavall-hytale-resource-game.jar com.tavall.hytale.resourcegame.controlserver.web.ControlServerApplication > logs/control-web.out.log 2> logs/control-web.err.log < /dev/null &
for i in `$(seq 1 60); do
  if ss -ltn | grep -q ':$ControlPort '; then
    sleep 2
    exit 0
  fi
  sleep 1
done
echo 'Resource-game control ingress did not become reachable.' >&2
tail -n 120 logs/control-web.err.log >&2 || true
tail -n 120 logs/control-web.out.log >&2 || true
exit 1
"@
Invoke-Checked -FilePath "ssh.exe" -Arguments @("-F", $SshConfigPath, $SshAlias, $remoteControl) -FailureMessage "Failed to start remote resource-game control ingress."

Write-LogLine "[$((Get-Date).ToString("o"))] Deploying Minecraft Velocity control-plane plugin."
Invoke-Checked -FilePath "ssh.exe" -Arguments @("-F", $SshConfigPath, $SshAlias, "mkdir -p '$RemoteProxyDir/plugins' '$RemoteProxyDir/logs' '$RemoteBackendDir/plugins' '$RemoteBackendDir/logs' '$RemoteSwitchBackendDir/plugins' '$RemoteSwitchBackendDir/logs' '$RemoteKingdomBackendDir/plugins' '$RemoteKingdomBackendDir/logs' '$RemoteHeadlessDir'") -FailureMessage "Failed to prepare remote Minecraft directories."
Invoke-Checked -FilePath "scp.exe" -Arguments @("-F", $SshConfigPath, $PluginJarPath, "$SshAlias`:$remotePluginPath.new") -FailureMessage "Failed to copy Velocity plugin jar."
Invoke-Checked -FilePath "scp.exe" -Arguments @("-F", $SshConfigPath, $ServerPluginJarPath, "$SshAlias`:$remoteBackendPluginPath.new") -FailureMessage "Failed to copy Bukkit server plugin jar to backend."
Invoke-Checked -FilePath "scp.exe" -Arguments @("-F", $SshConfigPath, $ServerPluginJarPath, "$SshAlias`:$remoteSwitchBackendPluginPath.new") -FailureMessage "Failed to copy Bukkit server plugin jar to switch backend."
Invoke-Checked -FilePath "scp.exe" -Arguments @("-F", $SshConfigPath, $ServerPluginJarPath, "$SshAlias`:$remoteKingdomBackendPluginPath.new") -FailureMessage "Failed to copy Bukkit server plugin jar to kingdom backend."
Invoke-Checked -FilePath "scp.exe" -Arguments @("-F", $SshConfigPath, $ScenarioScriptPath, "$SshAlias`:$remoteScriptPath") -FailureMessage "Failed to copy Minecraft Velocity scenario script."

$remoteDeploy = @"
set -euo pipefail
if [ -f '$remotePluginPath' ]; then
  cp '$remotePluginPath' '$remotePluginPath.bak-$timestamp'
fi
mv '$remotePluginPath.new' '$remotePluginPath'
if [ -f '$remoteBackendPluginPath' ]; then
  cp '$remoteBackendPluginPath' '$remoteBackendPluginPath.bak-$timestamp'
fi
mv '$remoteBackendPluginPath.new' '$remoteBackendPluginPath'
if [ -f '$remoteSwitchBackendPluginPath' ]; then
  cp '$remoteSwitchBackendPluginPath' '$remoteSwitchBackendPluginPath.bak-$timestamp'
fi
mv '$remoteSwitchBackendPluginPath.new' '$remoteSwitchBackendPluginPath'
if [ -f '$remoteKingdomBackendPluginPath' ]; then
  cp '$remoteKingdomBackendPluginPath' '$remoteKingdomBackendPluginPath.bak-$timestamp'
fi
mv '$remoteKingdomBackendPluginPath.new' '$remoteKingdomBackendPluginPath'
chmod +x '$RemoteProxyDir/start.sh' || true
python3 - <<'PY'
from pathlib import Path
path = Path('$RemoteProxyDir') / 'velocity.toml'
text = path.read_text()
lines = []
in_servers = False
server_written = False
for line in text.splitlines():
    stripped = line.strip()
    if stripped == '[servers]':
        in_servers = True
        lines.append(line)
        continue
    if in_servers and stripped.startswith('['):
        if not server_written:
            lines.append('$KingdomVelocityServerName = ' + chr(34) + '127.0.0.1:$KingdomBackendPort' + chr(34))
            server_written = True
        in_servers = False
    if line.strip().startswith('ffa ='):
        lines.append('ffa = ' + chr(34) + '127.0.0.1:$SwitchBackendPort' + chr(34))
    elif line.strip().startswith('$KingdomVelocityServerName ='):
        lines.append('$KingdomVelocityServerName = ' + chr(34) + '127.0.0.1:$KingdomBackendPort' + chr(34))
        server_written = True
    else:
        lines.append(line)
if in_servers and not server_written:
    lines.append('$KingdomVelocityServerName = ' + chr(34) + '127.0.0.1:$KingdomBackendPort' + chr(34))
path.write_text('\n'.join(lines) + '\n')
PY
"@
Invoke-Checked -FilePath "ssh.exe" -Arguments @("-F", $SshConfigPath, $SshAlias, $remoteDeploy) -FailureMessage "Failed to install remote Minecraft proxy/server plugins."

Write-LogLine "[$((Get-Date).ToString("o"))] Ensuring configured remote Minecraft backend is listening on 25566."
$remoteBackend = @"
set -euo pipefail
if [ ! -d '$RemoteBackendDir' ]; then
  echo 'Remote Minecraft backend directory does not exist: $RemoteBackendDir' >&2
  exit 1
fi
cd '$RemoteBackendDir'
mkdir -p logs plugins
if command -v fuser >/dev/null 2>&1; then
  fuser -k 25566/tcp 2>/dev/null || true
fi
for i in `$(seq 1 30); do
  if ! ss -ltn | grep -q ':25566 '; then
    break
  fi
  sleep 1
done
if ss -ltn | grep -q ':25566 '; then
  echo 'Minecraft backend port 25566 is still occupied after shutdown.' >&2
  ss -ltnp | grep ':25566 ' >&2 || true
  exit 1
fi
if [ -x /usr/lib/jvm/java-1.8.0-openjdk-arm64/bin/java ]; then
  JAVA_BIN=/usr/lib/jvm/java-1.8.0-openjdk-arm64/bin/java
else
  JAVA_BIN=java
fi
nohup env RESOURCE_GAME_MINECRAFT_CONTROL_INGRESS_URL='$ControlIngressUrl' RESOURCE_GAME_MINECRAFT_CONTROL_SNAPSHOT_URL='http://127.0.0.1:$ControlPort/api/frontend/minecraft/server-snapshots' RESOURCE_GAME_MINECRAFT_SERVER_ID='minecraft-backend-ffa' RESOURCE_GAME_MINECRAFT_PROXY_ID='velocity-proxy' "`$JAVA_BIN" -Xss1650k -Xmx1536M -jar spigot.jar nogui > logs/resource-game-backend.out.log 2> logs/resource-game-backend.err.log < /dev/null &
for i in `$(seq 1 60); do
  if ss -ltn | grep -q ':25566 '; then
    break
  fi
  sleep 1
done
if ! ss -ltn | grep -q ':25566 '; then
  echo 'Minecraft backend did not open port 25566 in time.' >&2
  tail -n 120 logs/resource-game-backend.err.log >&2 || true
  tail -n 120 logs/resource-game-backend.out.log >&2 || true
  exit 1
fi
for i in `$(seq 1 30); do
  if grep -h 'Tavall Resource Game Bukkit server frontend enabled' logs/latest.log logs/resource-game-backend.out.log logs/resource-game-backend.err.log 2>/dev/null; then
    exit 0
  fi
  sleep 1
done
echo 'Minecraft backend started, but Tavall Bukkit server plugin did not report enabled.' >&2
tail -n 160 logs/latest.log >&2 || true
tail -n 120 logs/resource-game-backend.err.log >&2 || true
tail -n 120 logs/resource-game-backend.out.log >&2 || true
exit 1
"@
Invoke-Checked -FilePath "ssh.exe" -Arguments @("-F", $SshConfigPath, $SshAlias, $remoteBackend) -FailureMessage "Failed to ensure remote Minecraft backend."

Write-LogLine "[$((Get-Date).ToString("o"))] Ensuring remote Minecraft switch backend is listening on $SwitchBackendPort."
$remoteSwitchBackend = @"
set -euo pipefail
if [ ! -f '$RemoteBackendDir/spigot.jar' ]; then
  echo 'Remote Minecraft source backend jar does not exist: $RemoteBackendDir/spigot.jar' >&2
  exit 1
fi
mkdir -p '$RemoteSwitchBackendDir' '$RemoteSwitchBackendDir/logs'
cp '$RemoteBackendDir/spigot.jar' '$RemoteSwitchBackendDir/spigot.jar'
cp '$RemoteBackendDir/spigot.yml' '$RemoteSwitchBackendDir/spigot.yml' 2>/dev/null || true
cp '$RemoteBackendDir/bukkit.yml' '$RemoteSwitchBackendDir/bukkit.yml' 2>/dev/null || true
cat > '$RemoteSwitchBackendDir/eula.txt' <<'EOF'
eula=true
EOF
cat > '$RemoteSwitchBackendDir/server.properties' <<'EOF'
server-port=$SwitchBackendPort
server-ip=127.0.0.1
online-mode=false
motd=Resource Game Switch Backend
enable-command-block=true
white-list=false
spawn-protection=0
EOF
cd '$RemoteSwitchBackendDir'
mkdir -p plugins logs
if command -v fuser >/dev/null 2>&1; then
  fuser -k $SwitchBackendPort/tcp 2>/dev/null || true
fi
for i in `$(seq 1 30); do
  if ! ss -ltn | grep -q ':$SwitchBackendPort '; then
    break
  fi
  sleep 1
done
if ss -ltn | grep -q ':$SwitchBackendPort '; then
  echo 'Minecraft switch backend port $SwitchBackendPort is still occupied after shutdown.' >&2
  ss -ltnp | grep ':$SwitchBackendPort ' >&2 || true
  exit 1
fi
if [ -x /usr/lib/jvm/java-1.8.0-openjdk-arm64/bin/java ]; then
  JAVA_BIN=/usr/lib/jvm/java-1.8.0-openjdk-arm64/bin/java
else
  JAVA_BIN=java
fi
nohup env RESOURCE_GAME_MINECRAFT_CONTROL_INGRESS_URL='$ControlIngressUrl' RESOURCE_GAME_MINECRAFT_CONTROL_SNAPSHOT_URL='http://127.0.0.1:$ControlPort/api/frontend/minecraft/server-snapshots' RESOURCE_GAME_MINECRAFT_SERVER_ID='minecraft-backend-switch' RESOURCE_GAME_MINECRAFT_PROXY_ID='velocity-proxy' "`$JAVA_BIN" -Xss1650k -Xmx1024M -jar spigot.jar nogui > logs/resource-game-switch-backend.out.log 2> logs/resource-game-switch-backend.err.log < /dev/null &
for i in `$(seq 1 60); do
  if ss -ltn | grep -q ':$SwitchBackendPort '; then
    break
  fi
  sleep 1
done
if ! ss -ltn | grep -q ':$SwitchBackendPort '; then
  echo 'Minecraft switch backend did not open port $SwitchBackendPort in time.' >&2
  tail -n 120 logs/resource-game-switch-backend.err.log >&2 || true
  tail -n 120 logs/resource-game-switch-backend.out.log >&2 || true
  exit 1
fi
for i in `$(seq 1 30); do
  if grep -h 'Tavall Resource Game Bukkit server frontend enabled' logs/latest.log logs/resource-game-switch-backend.out.log logs/resource-game-switch-backend.err.log 2>/dev/null; then
    exit 0
  fi
  sleep 1
done
echo 'Minecraft switch backend started, but Tavall Bukkit server plugin did not report enabled.' >&2
tail -n 160 logs/latest.log >&2 || true
tail -n 120 logs/resource-game-switch-backend.err.log >&2 || true
tail -n 120 logs/resource-game-switch-backend.out.log >&2 || true
exit 1
"@
Invoke-Checked -FilePath "ssh.exe" -Arguments @("-F", $SshConfigPath, $SshAlias, $remoteSwitchBackend) -FailureMessage "Failed to ensure remote Minecraft switch backend."

Write-LogLine "[$((Get-Date).ToString("o"))] Ensuring remote Minecraft kingdom backend is listening on $KingdomBackendPort."
$remoteKingdomBackend = @"
set -euo pipefail
if [ ! -f '$RemoteBackendDir/spigot.jar' ]; then
  echo 'Remote Minecraft source backend jar does not exist: $RemoteBackendDir/spigot.jar' >&2
  exit 1
fi
mkdir -p '$RemoteKingdomServersDir' '$RemoteKingdomBackendDir' '$RemoteKingdomBackendDir/logs' '$RemoteKingdomBackendDir/plugins'
cp '$RemoteBackendDir/spigot.jar' '$RemoteKingdomBackendDir/spigot.jar'
cp '$RemoteBackendDir/spigot.yml' '$RemoteKingdomBackendDir/spigot.yml' 2>/dev/null || true
cp '$RemoteBackendDir/bukkit.yml' '$RemoteKingdomBackendDir/bukkit.yml' 2>/dev/null || true
cat > '$RemoteKingdomBackendDir/eula.txt' <<'EOF'
eula=true
EOF
cat > '$RemoteKingdomBackendDir/server.properties' <<'EOF'
server-port=$KingdomBackendPort
server-ip=127.0.0.1
online-mode=false
motd=Resource Game Kingdom Server 1
enable-command-block=true
white-list=false
spawn-protection=0
EOF
cd '$RemoteKingdomBackendDir'
if command -v fuser >/dev/null 2>&1; then
  fuser -k $KingdomBackendPort/tcp 2>/dev/null || true
fi
for i in `$(seq 1 30); do
  if ! ss -ltn | grep -q ':$KingdomBackendPort '; then
    break
  fi
  sleep 1
done
if ss -ltn | grep -q ':$KingdomBackendPort '; then
  echo 'Minecraft kingdom backend port $KingdomBackendPort is still occupied after shutdown.' >&2
  ss -ltnp | grep ':$KingdomBackendPort ' >&2 || true
  exit 1
fi
if [ -x /usr/lib/jvm/java-1.8.0-openjdk-arm64/bin/java ]; then
  JAVA_BIN=/usr/lib/jvm/java-1.8.0-openjdk-arm64/bin/java
else
  JAVA_BIN=java
fi
nohup env RESOURCE_GAME_MINECRAFT_CONTROL_INGRESS_URL='$ControlIngressUrl' RESOURCE_GAME_MINECRAFT_CONTROL_SNAPSHOT_URL='http://127.0.0.1:$ControlPort/api/frontend/minecraft/server-snapshots' RESOURCE_GAME_MINECRAFT_SERVER_ID='$RemoteKingdomBackendName' RESOURCE_GAME_MINECRAFT_PROXY_ID='velocity-proxy' "`$JAVA_BIN" -Xss1650k -Xmx1024M -jar spigot.jar nogui > logs/resource-game-kingdom-backend.out.log 2> logs/resource-game-kingdom-backend.err.log < /dev/null &
for i in `$(seq 1 60); do
  if ss -ltn | grep -q ':$KingdomBackendPort '; then
    break
  fi
  sleep 1
done
if ! ss -ltn | grep -q ':$KingdomBackendPort '; then
  echo 'Minecraft kingdom backend did not open port $KingdomBackendPort in time.' >&2
  tail -n 120 logs/resource-game-kingdom-backend.err.log >&2 || true
  tail -n 120 logs/resource-game-kingdom-backend.out.log >&2 || true
  exit 1
fi
for i in `$(seq 1 30); do
  if grep -h 'Tavall Resource Game Bukkit server frontend enabled' logs/latest.log logs/resource-game-kingdom-backend.out.log logs/resource-game-kingdom-backend.err.log 2>/dev/null; then
    exit 0
  fi
  sleep 1
done
echo 'Minecraft kingdom backend started, but Tavall Bukkit server plugin did not report enabled.' >&2
tail -n 160 logs/latest.log >&2 || true
tail -n 120 logs/resource-game-kingdom-backend.err.log >&2 || true
tail -n 120 logs/resource-game-kingdom-backend.out.log >&2 || true
exit 1
"@
Invoke-Checked -FilePath "ssh.exe" -Arguments @("-F", $SshConfigPath, $SshAlias, $remoteKingdomBackend) -FailureMessage "Failed to ensure remote Minecraft kingdom backend."

Write-LogLine "[$((Get-Date).ToString("o"))] Restarting remote Velocity proxy."
$remoteRestart = @"
set -euo pipefail
test -f '$remotePluginPath'
test -f '$remoteBackendPluginPath'
test -f '$remoteSwitchBackendPluginPath'
test -f '$remoteKingdomBackendPluginPath'
pkill -f '[v]elocity.jar' || true
for i in `$(seq 1 30); do
  if ! ss -ltn | grep -q ':25565 '; then
    break
  fi
  sleep 1
done
cd '$RemoteProxyDir'
nohup env RESOURCE_GAME_MINECRAFT_CONTROL_INGRESS_URL='$ControlIngressUrl' RESOURCE_GAME_MINECRAFT_OWNER_USERNAMES='$BotUsername' RESOURCE_GAME_MINECRAFT_SERVER_ID='velocity-proxy' RESOURCE_GAME_MINECRAFT_INSTANCE_SERVER_MAP='$InstanceServerMap' bash ./start.sh > logs/resource-game-proxy.out.log 2> logs/resource-game-proxy.err.log < /dev/null &
for i in `$(seq 1 40); do
  if ss -ltn | grep -q ':25565 '; then
    sleep 2
    exit 0
  fi
  sleep 1
done
echo 'Velocity proxy did not open port 25565 in time.' >&2
tail -n 80 logs/resource-game-proxy.err.log >&2 || true
tail -n 80 logs/resource-game-proxy.out.log >&2 || true
exit 1
"@
Invoke-Checked -FilePath "ssh.exe" -Arguments @("-F", $SshConfigPath, $SshAlias, $remoteRestart) -FailureMessage "Failed to restart remote Velocity proxy."

Write-LogLine "[$((Get-Date).ToString("o"))] Whitelisting bot if remote whitelist helper exists."
$remoteWhitelist = "if [ -x '$RemoteProxyDir/ensure-proxy-whitelist-users.sh' ]; then '$RemoteProxyDir/ensure-proxy-whitelist-users.sh' '$BotUsername'; fi"
Invoke-Checked -FilePath "ssh.exe" -Arguments @("-F", $SshConfigPath, $SshAlias, $remoteWhitelist) -FailureMessage "Failed to whitelist Minecraft verification bot."

Write-LogLine "[$((Get-Date).ToString("o"))] Running Minecraft Velocity command flow through the remote proxy."
$remoteRun = "cd '$RemoteHeadlessDir' && mkdir -p '$remoteOutputDir' && MINECRAFT_PROXY_COMMANDS='$CommandList' node '$remoteScriptPath' 127.0.0.1 25565 '$BotUsername' '$MinecraftVersion' '$remoteOutputDir'"
Invoke-Checked -FilePath "ssh.exe" -Arguments @("-F", $SshConfigPath, $SshAlias, $remoteRun) -FailureMessage "Remote Minecraft Velocity command flow failed."

Invoke-Checked -FilePath "scp.exe" -Arguments @("-F", $SshConfigPath, "$SshAlias`:$remoteOutputDir/scenario-result.json", $resultPath) -FailureMessage "Failed to copy remote Minecraft Velocity result."
Invoke-Checked -FilePath "scp.exe" -Arguments @("-F", $SshConfigPath, "$SshAlias`:$remoteOutputDir/transcript.txt", $transcriptPath) -FailureMessage "Failed to copy remote Minecraft Velocity transcript."

Write-LogLine "[$((Get-Date).ToString("o"))] ResultFile=$resultPath"
Write-LogLine "[$((Get-Date).ToString("o"))] TranscriptFile=$transcriptPath"
