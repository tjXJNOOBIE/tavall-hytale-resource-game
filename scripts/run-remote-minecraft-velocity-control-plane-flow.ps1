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
    [string]$KingdomMinecraftVersion = "1.21.4",
    [string]$KingdomServerJarName = "paper-1.21.4.jar",
    [string]$KingdomServerJarLocalPath = "",
    [string]$RemoteControlDir = "/srv/resource-game-control",
    [string]$RemoteHeadlessDir = "/srv/headless",
    [string]$RemotePublicResourcePackDir = "/var/www/html/resource-game/minecraft",
    [string]$ControlServerJarPath = "F:/workspace/TavallMonoRepo/tavall-java-hytale-games/tavall-hytale-resource-game/distribution/control-server/application.jar",
    [string]$PluginJarPath = "F:/workspace/TavallMonoRepo/tavall-java-hytale-games/tavall-hytale-resource-game/distribution/minecraft-proxy/plugins/minecraft-proxy.jar",
    [string]$ServerPluginJarPath = "F:/workspace/TavallMonoRepo/tavall-java-hytale-games/tavall-hytale-resource-game/distribution/minecraft-game-server/plugins/minecraft-game-server.jar",
    [string]$BundledResourcePackPath = "F:/workspace/TavallMonoRepo/tavall-java-hytale-games/tavall-hytale-resource-game/resource-pack/distribution/crownbound_minecraft_resource_pack.zip",
    [string]$BundledResourcePackChecksumPath = "F:/workspace/TavallMonoRepo/tavall-java-hytale-games/tavall-hytale-resource-game/resource-pack/distribution/crownbound_minecraft_resource_pack.sha256.txt",
    [string]$ResourcePackRootPath = "F:/workspace/TavallMonoRepo/tavall-java-hytale-games/tavall-hytale-resource-game/resource-pack",
    [string]$ResourcePackExtractorScriptPath = "F:/workspace/TavallMonoRepo/tavall-java-hytale-games/tavall-hytale-resource-game/scripts/extract-crownbound-minecraft-ui-item-models.py",
    [string]$GuiThemeBuilderScriptPath = "F:/workspace/TavallMonoRepo/tavall-java-hytale-games/tavall-hytale-resource-game/scripts/build-crownbound-minecraft-gui-theme.py",
    [string]$RuntimeResourcePackBuilderScriptPath = "F:/workspace/TavallMonoRepo/tavall-java-hytale-games/tavall-hytale-resource-game/scripts/build-crownbound-minecraft-runtime-pack.py",
    [string]$PublicResourcePackUrl = "https://docs.tavall.org/resource-game/minecraft/resource-pack.zip",
    [string]$ScenarioScriptPath = "F:/workspace/TavallMonoRepo/tavall-java-hytale-games/tavall-hytale-resource-game/scripts/minecraft-velocity-control-plane-flow.mjs",
    [int]$ControlPort = 19081,
    [string]$BotUsername = "ResourceProxyBot",
    [string]$MinecraftVersion = "1.21.4",
    [string]$InstanceServerMap = "kingdom-1-minecraft-primary=kingdom,kingdom-2-minecraft-primary=ffa",
    [string]$CommandList = "/server kingdom|/kd help|/kd ui|/kd place castle|/kd place confirm|/kd buildings stage farmstead|/kd hologram spawn kingdom-debug|/kd entity spawn farmer|/kd resources add food 3|/kd companion give ARCANE|/kd scene refresh",
    [string]$ServerCommandList = "/kd help|/kd ui|/kd place castle|/kd place confirm|/kd buildings stage farmstead|/kd hologram spawn kingdom-debug|/kd entity spawn farmer|/kd interior add|/kd resources add food 3|/kd companion give ARCANE|/kd scene refresh",
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
$serverResultPath = Join-Path $logRoot "$baseName-server-result.json"
$serverTranscriptPath = Join-Path $logRoot "$baseName-server-transcript.txt"
$remotePluginPath = "$RemoteProxyDir/plugins/minecraft-proxy.jar"
$remoteBackendPluginPath = "$RemoteBackendDir/plugins/minecraft-game-server.jar"
$remoteSwitchBackendPluginPath = "$RemoteSwitchBackendDir/plugins/minecraft-game-server.jar"
$RemoteKingdomBackendDir = "$RemoteKingdomServersDir/$RemoteKingdomBackendName"
$remoteKingdomBackendPluginPath = "$RemoteKingdomBackendDir/plugins/minecraft-game-server.jar"
$remoteKingdomServerJarPath = "$RemoteKingdomBackendDir/$KingdomServerJarName"
$remoteScriptPath = "$RemoteHeadlessDir/$baseName.mjs"
$remoteOutputDir = "/tmp/$baseName"
$remoteBundledPackPath = "/tmp/$baseName-resource-pack.zip"
$remoteBundledPackChecksumPath = "/tmp/$baseName-resource-pack.sha256.txt"
$remoteBundledPackSha1Path = "/tmp/$baseName-resource-pack.sha1.txt"
$runtimeResourcePackPath = Join-Path $logRoot "$baseName-resource-pack.zip"
$runtimeResourcePackChecksumPath = Join-Path $logRoot "$baseName-resource-pack.sha256.txt"
$runtimeResourcePackSha1Path = Join-Path $logRoot "$baseName-resource-pack.sha1.txt"

function Write-LogLine {
    param([string]$Message)
    Add-Content -Path $runLogPath -Value $Message -Encoding utf8
    Write-Host $Message
}

function Invoke-Checked {
    param([string]$FilePath, [string[]]$Arguments, [string]$FailureMessage)
    $normalizedArguments = $Arguments | ForEach-Object {
        if ($_ -is [string]) {
            $_ -replace "`r`n", "`n"
        } else {
            $_
        }
    }
    & $FilePath @normalizedArguments 2>&1 | Tee-Object -FilePath $runLogPath -Append
    if ($LASTEXITCODE -ne 0) {
        throw $FailureMessage
    }
}

$ResourcePackUrl = $PublicResourcePackUrl

if (-not (Test-Path $PluginJarPath)) {
    throw "Minecraft Velocity plugin jar not found at $PluginJarPath"
}
if (-not (Test-Path $ServerPluginJarPath)) {
    throw "Minecraft Bukkit server plugin jar not found at $ServerPluginJarPath"
}
if (-not (Test-Path $BundledResourcePackPath)) {
    throw "Bundled resource pack not found at $BundledResourcePackPath"
}
if (-not (Test-Path $BundledResourcePackChecksumPath)) {
    throw "Bundled resource pack checksum not found at $BundledResourcePackChecksumPath"
}
if (-not (Test-Path $ResourcePackExtractorScriptPath)) {
    throw "Resource pack extractor script not found at $ResourcePackExtractorScriptPath"
}
if (-not (Test-Path $GuiThemeBuilderScriptPath)) {
    throw "GUI theme builder script not found at $GuiThemeBuilderScriptPath"
}
if (-not (Test-Path $RuntimeResourcePackBuilderScriptPath)) {
    throw "Runtime resource pack builder script not found at $RuntimeResourcePackBuilderScriptPath"
}
if (-not (Test-Path $ControlServerJarPath)) {
    throw "Control server jar not found at $ControlServerJarPath"
}
$controlServerLibDirectory = Join-Path (Split-Path -Parent $ControlServerJarPath) "libs"
if (-not (Test-Path $controlServerLibDirectory -PathType Container)) {
    throw "Control server dependency directory not found at $controlServerLibDirectory"
}
if (-not (Test-Path $ScenarioScriptPath)) {
    throw "Scenario script not found at $ScenarioScriptPath"
}
if ([string]::IsNullOrWhiteSpace($KingdomServerJarLocalPath)) {
    $paperCacheDir = Join-Path $logRoot "paper"
    New-Item -ItemType Directory -Force -Path $paperCacheDir | Out-Null
    $KingdomServerJarLocalPath = Join-Path $paperCacheDir $KingdomServerJarName
}
if (-not (Test-Path $KingdomServerJarLocalPath)) {
    Write-LogLine "[$((Get-Date).ToString("o"))] Downloading Paper $KingdomMinecraftVersion for kingdom backend."
    $builds = Invoke-RestMethod -Uri "https://api.papermc.io/v2/projects/paper/versions/$KingdomMinecraftVersion/builds"
    $build = $builds.builds[-1].build
    $download = (Invoke-RestMethod -Uri "https://api.papermc.io/v2/projects/paper/versions/$KingdomMinecraftVersion/builds/$build").downloads.application.name
    Invoke-WebRequest -Uri "https://api.papermc.io/v2/projects/paper/versions/$KingdomMinecraftVersion/builds/$build/downloads/$download" -OutFile "$KingdomServerJarLocalPath.new"
    Move-Item -Force -Path "$KingdomServerJarLocalPath.new" -Destination $KingdomServerJarLocalPath
}

Write-LogLine "[$((Get-Date).ToString("o"))] Generating runtime Minecraft UI item textures."
Invoke-Checked -FilePath "cmd.exe" -Arguments @(
    "/c",
    "python `"$ResourcePackExtractorScriptPath`""
) -FailureMessage "Failed to generate runtime Minecraft UI item textures."

Write-LogLine "[$((Get-Date).ToString("o"))] Building Crownbound Minecraft GUI theme assets."
Invoke-Checked -FilePath "cmd.exe" -Arguments @(
    "/c",
    "python `"$GuiThemeBuilderScriptPath`""
) -FailureMessage "Failed to build the Crownbound Minecraft GUI theme."

Write-LogLine "[$((Get-Date).ToString("o"))] Validating Crownbound Minecraft GUI alignment."
Invoke-Checked -FilePath "cmd.exe" -Arguments @(
    "/c",
    "python `"${PSScriptRoot}\validate-crownbound-minecraft-gui-alignment.py`" --texture `"$repoRoot\resource-pack\assets\minecraft\textures\gui\container\generic_54.png`" --tolerance 1.5 --report `"$logRoot\${baseName}-gui-alignment.json`""
) -FailureMessage "Failed to validate the Crownbound Minecraft GUI alignment."

Write-LogLine "[$((Get-Date).ToString("o"))] Building runtime resource pack archive."
Invoke-Checked -FilePath "cmd.exe" -Arguments @(
    "/c",
    "python `"$RuntimeResourcePackBuilderScriptPath`" --root `"$ResourcePackRootPath`" --bundle `"$BundledResourcePackPath`" --output `"$runtimeResourcePackPath`" --checksum `"$runtimeResourcePackChecksumPath`" --sha1-checksum `"$runtimeResourcePackSha1Path`""
) -FailureMessage "Failed to build runtime resource pack archive."

$runtimeResourcePackHash = ((Get-Content -Path $runtimeResourcePackChecksumPath -TotalCount 1) -split '\s+')[0].Trim().ToLowerInvariant()
if ([string]::IsNullOrWhiteSpace($runtimeResourcePackHash)) {
    throw "Runtime resource pack checksum was empty at $runtimeResourcePackChecksumPath"
}
$resourcePackUriBuilder = [System.UriBuilder]::new($PublicResourcePackUrl)
$resourcePackUriBuilder.Query = "v=$runtimeResourcePackHash"
$ResourcePackUrl = $resourcePackUriBuilder.Uri.AbsoluteUri
Write-LogLine "[$((Get-Date).ToString("o"))] Using versioned public resource-pack URL: $ResourcePackUrl"

Write-LogLine "[$((Get-Date).ToString("o"))] Deploying resource-game control bridge."
Invoke-Checked -FilePath "ssh.exe" -Arguments @("-F", $SshConfigPath, $SshAlias, "mkdir -p '$RemoteControlDir/logs' && rm -rf '$RemoteControlDir/libs.new'") -FailureMessage "Failed to prepare remote control directory."
Invoke-Checked -FilePath "scp.exe" -Arguments @("-F", $SshConfigPath, $ControlServerJarPath, "$SshAlias`:$RemoteControlDir/control-server.jar.new") -FailureMessage "Failed to copy control server jar."
Invoke-Checked -FilePath "scp.exe" -Arguments @("-F", $SshConfigPath, "-r", $controlServerLibDirectory, "$SshAlias`:$RemoteControlDir/libs.new") -FailureMessage "Failed to copy control server dependencies."
$remoteControl = @"
set -euo pipefail
cd '$RemoteControlDir'
mv control-server.jar.new control-server.jar
rm -rf libs.previous
if [ -d libs ]; then mv libs libs.previous; fi
mv libs.new libs
if command -v fuser >/dev/null 2>&1; then
  fuser -k $ControlPort/tcp 2>/dev/null || true
fi
for i in `$(seq 1 30); do
  if ! ss -ltn | grep -q ':$ControlPort '; then
    break
  fi
  sleep 1
done
if ss -ltn | grep -q ':$ControlPort '; then
  lsof -ti tcp:$ControlPort | xargs -r kill -9 || true
fi
for i in `$(seq 1 10); do
  if ! ss -ltn | grep -q ':$ControlPort '; then
    break
  fi
  sleep 1
done
if ss -ltn | grep -q ':$ControlPort '; then
  echo 'Resource-game control bridge port is still occupied after shutdown.' >&2
  ss -ltnp | grep ':$ControlPort ' >&2 || true
  exit 1
fi
tmux kill-session -t control 2>/dev/null || true
tmux new-session -d -s control -c '$RemoteControlDir' "env TAVALL_CONTROL_BRIDGE_HOST=127.0.0.1 TAVALL_CONTROL_BRIDGE_PORT=$ControlPort java --enable-preview -cp 'control-server.jar:libs/*' org.tavall.control.cli.ControlConsoleApplication 2>&1 | tee -a logs/control-bridge.out.log"
for i in `$(seq 1 60); do
  if ss -ltn | grep -q ':$ControlPort '; then
    sleep 2
    exit 0
  fi
  sleep 1
done
echo 'Resource-game control bridge did not become reachable.' >&2
tail -n 120 logs/control-bridge.err.log >&2 || true
tail -n 120 logs/control-bridge.out.log >&2 || true
exit 1
"@
Invoke-Checked -FilePath "ssh.exe" -Arguments @("-F", $SshConfigPath, $SshAlias, $remoteControl) -FailureMessage "Failed to start remote resource-game control bridge."

Write-LogLine "[$((Get-Date).ToString("o"))] Deploying Minecraft Velocity control-plane plugin."
Invoke-Checked -FilePath "ssh.exe" -Arguments @("-F", $SshConfigPath, $SshAlias, "mkdir -p '$RemoteProxyDir/plugins' '$RemoteProxyDir/logs' '$RemoteBackendDir/plugins' '$RemoteBackendDir/logs' '$RemoteSwitchBackendDir/plugins' '$RemoteSwitchBackendDir/logs' '$RemoteKingdomBackendDir/plugins' '$RemoteKingdomBackendDir/logs' '$RemoteHeadlessDir'") -FailureMessage "Failed to prepare remote Minecraft directories."
Invoke-Checked -FilePath "scp.exe" -Arguments @("-F", $SshConfigPath, $PluginJarPath, "$SshAlias`:$remotePluginPath.new") -FailureMessage "Failed to copy Velocity plugin jar."
Invoke-Checked -FilePath "scp.exe" -Arguments @("-F", $SshConfigPath, $ServerPluginJarPath, "$SshAlias`:$remoteBackendPluginPath.new") -FailureMessage "Failed to copy Bukkit server plugin jar to backend."
Invoke-Checked -FilePath "scp.exe" -Arguments @("-F", $SshConfigPath, $ServerPluginJarPath, "$SshAlias`:$remoteSwitchBackendPluginPath.new") -FailureMessage "Failed to copy Bukkit server plugin jar to switch backend."
Invoke-Checked -FilePath "scp.exe" -Arguments @("-F", $SshConfigPath, $ServerPluginJarPath, "$SshAlias`:$remoteKingdomBackendPluginPath.new") -FailureMessage "Failed to copy Bukkit server plugin jar to kingdom backend."
Invoke-Checked -FilePath "scp.exe" -Arguments @("-F", $SshConfigPath, $runtimeResourcePackPath, "$SshAlias`:$remoteBundledPackPath") -FailureMessage "Failed to copy runtime resource pack."
Invoke-Checked -FilePath "scp.exe" -Arguments @("-F", $SshConfigPath, $runtimeResourcePackChecksumPath, "$SshAlias`:$remoteBundledPackChecksumPath") -FailureMessage "Failed to copy runtime resource pack checksum."
Invoke-Checked -FilePath "scp.exe" -Arguments @("-F", $SshConfigPath, $runtimeResourcePackSha1Path, "$SshAlias`:$remoteBundledPackSha1Path") -FailureMessage "Failed to copy runtime resource pack sha1 checksum."
Invoke-Checked -FilePath "scp.exe" -Arguments @("-F", $SshConfigPath, $KingdomServerJarLocalPath, "$SshAlias`:$remoteKingdomServerJarPath.new") -FailureMessage "Failed to copy Paper kingdom backend jar."
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
mkdir -p '$RemoteKingdomBackendDir/resource-pack/distribution'
cp '$remoteBundledPackPath' '$RemoteKingdomBackendDir/resource-pack/distribution/crownbound_minecraft_resource_pack.zip'
cp '$remoteBundledPackChecksumPath' '$RemoteKingdomBackendDir/resource-pack/distribution/crownbound_minecraft_resource_pack.sha256.txt'
cp '$remoteBundledPackSha1Path' '$RemoteKingdomBackendDir/resource-pack/distribution/crownbound_minecraft_resource_pack.sha1.txt'
sudo install -d -m 755 '$RemotePublicResourcePackDir'
sudo install -m 644 '$remoteBundledPackPath' '$RemotePublicResourcePackDir/resource-pack.zip'
sudo install -m 644 '$remoteBundledPackChecksumPath' '$RemotePublicResourcePackDir/resource-pack.sha256.txt'
sudo install -m 644 '$remoteBundledPackSha1Path' '$RemotePublicResourcePackDir/resource-pack.sha1.txt'
rm -f '$RemoteKingdomBackendDir/plugins/tavall-resource-game-minecraft-server-frontend.jar' 2>/dev/null || true
if [ -f '$remoteKingdomServerJarPath' ]; then
  cp '$remoteKingdomServerJarPath' '$remoteKingdomServerJarPath.bak-$timestamp'
fi
mv '$remoteKingdomServerJarPath.new' '$remoteKingdomServerJarPath'
chmod +x '$RemoteProxyDir/start.sh' || true
python3 - <<'PY'
from pathlib import Path
path = Path('$RemoteProxyDir') / 'velocity.toml'
text = path.read_text()
quote = chr(34)
text = '\n'.join(
    'player-info-forwarding-mode = ' + quote + 'none' + quote if line.strip().startswith('player-info-forwarding-mode') else line
    for line in text.splitlines()
) + '\n'
lines = []
in_servers = False
server_written = False
skip_try = False
for line in text.splitlines():
    stripped = line.strip()
    if skip_try:
        if stripped == ']':
            lines.append('try = [')
            lines.append('    ' + chr(34) + '$KingdomVelocityServerName' + chr(34) + ',')
            lines.append('    ' + chr(34) + 'lobby' + chr(34))
            lines.append(']')
            skip_try = False
        continue
    if stripped == '[servers]':
        in_servers = True
        lines.append(line)
        continue
    if in_servers and stripped.startswith('['):
        if not server_written:
            lines.append('$KingdomVelocityServerName = ' + chr(34) + '127.0.0.1:$KingdomBackendPort' + chr(34))
            server_written = True
        in_servers = False
    if in_servers and stripped.startswith('try ='):
        if not server_written:
            lines.append('$KingdomVelocityServerName = ' + chr(34) + '127.0.0.1:$KingdomBackendPort' + chr(34))
            server_written = True
        skip_try = True
        continue
    if line.strip().startswith('ffa ='):
        lines.append('ffa = ' + chr(34) + '127.0.0.1:$SwitchBackendPort' + chr(34))
    elif line.strip().startswith('$KingdomVelocityServerName ='):
        if not server_written:
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

Write-LogLine "[$((Get-Date).ToString("o"))] Verifying public resource-pack URL."
$publicResourcePackProbe = @"
set -euo pipefail
curl --fail --silent --show-error --location '$ResourcePackUrl' --output /tmp/$baseName-public-resource-pack.zip
sha256sum /tmp/$baseName-public-resource-pack.zip
"@
Invoke-Checked -FilePath "ssh.exe" -Arguments @("-F", $SshConfigPath, $SshAlias, $publicResourcePackProbe) -FailureMessage "Failed to verify public resource-pack URL."

Write-LogLine "[$((Get-Date).ToString("o"))] Ensuring configured remote Minecraft backend is listening on 25566."
$remoteBackend = @"
set -euo pipefail
if [ ! -d '$RemoteBackendDir' ]; then
  echo 'Remote Minecraft backend directory does not exist: $RemoteBackendDir' >&2
  exit 1
fi
cd '$RemoteBackendDir'
mkdir -p logs plugins
python3 - <<'PY'
from pathlib import Path
path = Path('spigot.yml')
if path.exists():
    path.write_text(path.read_text().replace('  bungeecord: true', '  bungeecord: false'))
PY
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
JAVA_BIN=java
tmux kill-session -t minecraft-ffa 2>/dev/null || true
tmux new-session -d -s minecraft-ffa -c '$RemoteBackendDir' "env RESOURCE_GAME_MINECRAFT_SERVER_ID='minecraft-backend-ffa' RESOURCE_GAME_MINECRAFT_PROXY_ID='velocity-proxy' RESOURCE_GAME_MINECRAFT_CONTROL_INGRESS_URL='tcp://127.0.0.1:$ControlPort' RESOURCE_GAME_CONTROL_INGRESS_URL='tcp://127.0.0.1:$ControlPort' java -Xss1650k -Xmx1536M -jar spigot.jar nogui 2>&1 | tee -a logs/resource-game-backend.out.log"
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
  if grep -h 'Tavall Resource Game Bukkit server frontend enabled. serverId=minecraft-backend-ffa proxyId=velocity-proxy' logs/latest.log logs/resource-game-backend.out.log logs/resource-game-backend.err.log 2>/dev/null; then
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
python3 - <<'PY'
from pathlib import Path
path = Path('spigot.yml')
if path.exists():
    path.write_text(path.read_text().replace('  bungeecord: true', '  bungeecord: false'))
PY
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
JAVA_BIN=java
tmux kill-session -t minecraft-switch 2>/dev/null || true
tmux new-session -d -s minecraft-switch -c '$RemoteSwitchBackendDir' "env RESOURCE_GAME_MINECRAFT_SERVER_ID='minecraft-backend-switch' RESOURCE_GAME_MINECRAFT_PROXY_ID='velocity-proxy' RESOURCE_GAME_MINECRAFT_CONTROL_INGRESS_URL='tcp://127.0.0.1:$ControlPort' RESOURCE_GAME_CONTROL_INGRESS_URL='tcp://127.0.0.1:$ControlPort' java -Xss1650k -Xmx1024M -jar spigot.jar nogui 2>&1 | tee -a logs/resource-game-switch-backend.out.log"
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
  if grep -h 'Tavall Resource Game Bukkit server frontend enabled. serverId=minecraft-backend-switch proxyId=velocity-proxy' logs/latest.log logs/resource-game-switch-backend.out.log logs/resource-game-switch-backend.err.log 2>/dev/null; then
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
mkdir -p '$RemoteKingdomServersDir' '$RemoteKingdomBackendDir' '$RemoteKingdomBackendDir/logs' '$RemoteKingdomBackendDir/plugins'
cp '$RemoteBackendDir/spigot.yml' '$RemoteKingdomBackendDir/spigot.yml' 2>/dev/null || true
cp '$RemoteBackendDir/bukkit.yml' '$RemoteKingdomBackendDir/bukkit.yml' 2>/dev/null || true
if [ ! -f '$RemoteKingdomBackendDir/$KingdomServerJarName' ]; then
  echo 'Kingdom Paper jar missing after deploy: $RemoteKingdomBackendDir/$KingdomServerJarName' >&2
  exit 1
fi
ln -sfn '$KingdomServerJarName' '$RemoteKingdomBackendDir/server.jar'
cat > '$RemoteKingdomBackendDir/eula.txt' <<'EOF'
eula=true
EOF
cat > '$RemoteKingdomBackendDir/server.properties' <<'EOF'
server-port=$KingdomBackendPort
server-ip=127.0.0.1
online-mode=false
enforce-secure-profile=false
motd=Resource Game Kingdom Server 1
enable-command-block=true
white-list=false
spawn-protection=0
EOF
cd '$RemoteKingdomBackendDir'
python3 - <<'PY'
from pathlib import Path
path = Path('spigot.yml')
if path.exists():
    path.write_text(path.read_text().replace('  bungeecord: true', '  bungeecord: false'))
PY
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
JAVA_BIN=java
tmux kill-session -t minecraft-kingdom 2>/dev/null || true
tmux new-session -d -s minecraft-kingdom -c '$RemoteKingdomBackendDir' "env RESOURCE_GAME_MINECRAFT_SERVER_ID='$RemoteKingdomBackendName' RESOURCE_GAME_MINECRAFT_PROXY_ID='velocity-proxy' RESOURCE_GAME_MINECRAFT_CONTROL_INGRESS_URL='tcp://127.0.0.1:$ControlPort' RESOURCE_GAME_CONTROL_INGRESS_URL='tcp://127.0.0.1:$ControlPort' RESOURCE_GAME_MINECRAFT_RESOURCE_PACK_URL='$ResourcePackUrl' java -Xmx1536M -jar server.jar nogui 2>&1 | tee -a logs/resource-game-kingdom-backend.out.log"
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
for i in `$(seq 1 120); do
  if grep -h 'Tavall Resource Game Bukkit server frontend enabled. serverId=$RemoteKingdomBackendName proxyId=velocity-proxy' logs/latest.log logs/resource-game-kingdom-backend.out.log logs/resource-game-kingdom-backend.err.log 2>/dev/null; then
    if grep -h 'Minecraft server version: $KingdomMinecraftVersion' logs/latest.log logs/resource-game-kingdom-backend.out.log logs/resource-game-kingdom-backend.err.log 2>/dev/null || grep -h 'Starting minecraft server version $KingdomMinecraftVersion' logs/latest.log logs/resource-game-kingdom-backend.out.log logs/resource-game-kingdom-backend.err.log 2>/dev/null; then
      exit 0
    fi
  fi
  sleep 1
done
echo 'Minecraft kingdom backend started, but Tavall Bukkit server plugin or Paper $KingdomMinecraftVersion did not report enabled.' >&2
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
tmux kill-session -t minecraft-proxy 2>/dev/null || true
tmux new-session -d -s minecraft-proxy -c '$RemoteProxyDir' "env RESOURCE_GAME_MINECRAFT_OWNER_USERNAMES='$BotUsername' RESOURCE_GAME_MINECRAFT_SERVER_ID='velocity-proxy' RESOURCE_GAME_MINECRAFT_INSTANCE_SERVER_MAP='$InstanceServerMap' RESOURCE_GAME_MINECRAFT_CONTROL_INGRESS_URL='tcp://127.0.0.1:$ControlPort' RESOURCE_GAME_CONTROL_INGRESS_URL='tcp://127.0.0.1:$ControlPort' bash ./start.sh 2>&1 | tee -a logs/resource-game-proxy.out.log"
for i in `$(seq 1 40); do
  if ss -ltn | grep -q ':25565 '; then
    break
  fi
  sleep 1
done
if ! ss -ltn | grep -q ':25565 '; then
  echo 'Velocity proxy did not open port 25565 in time.' >&2
  tail -n 80 logs/resource-game-proxy.err.log >&2 || true
  tail -n 80 logs/resource-game-proxy.out.log >&2 || true
  exit 1
fi
for i in `$(seq 1 30); do
  if grep -h 'Registered Tavall Resource Game Velocity routing adapter. serverId=velocity-proxy' logs/latest.log logs/resource-game-proxy.out.log logs/resource-game-proxy.err.log 2>/dev/null; then
    exit 0
  fi
  sleep 1
done
echo 'Velocity proxy opened 25565, but updated Tavall proxy plugin did not report registered.' >&2
tail -n 120 logs/latest.log >&2 || true
tail -n 80 logs/resource-game-proxy.err.log >&2 || true
tail -n 80 logs/resource-game-proxy.out.log >&2 || true
exit 1
"@
Invoke-Checked -FilePath "ssh.exe" -Arguments @("-F", $SshConfigPath, $SshAlias, $remoteRestart) -FailureMessage "Failed to restart remote Velocity proxy."

Write-LogLine "[$((Get-Date).ToString("o"))] Whitelisting bot if remote whitelist helper exists."
$remoteWhitelist = "if [ -x '$RemoteProxyDir/ensure-proxy-whitelist-users.sh' ]; then '$RemoteProxyDir/ensure-proxy-whitelist-users.sh' '$BotUsername'; fi"
Invoke-Checked -FilePath "ssh.exe" -Arguments @("-F", $SshConfigPath, $SshAlias, $remoteWhitelist) -FailureMessage "Failed to whitelist Minecraft verification bot."

Write-LogLine "[$((Get-Date).ToString("o"))] Running Minecraft Velocity command flow through the remote proxy."
$remoteRun = "cd '$RemoteHeadlessDir' && mkdir -p '$remoteOutputDir' && MINECRAFT_PROXY_COMMANDS='$CommandList' MINECRAFT_PROXY_COMMAND_DELAY_MS=1000 node '$remoteScriptPath' 127.0.0.1 25565 '$BotUsername' '$MinecraftVersion' '$remoteOutputDir'"
Invoke-Checked -FilePath "ssh.exe" -Arguments @("-F", $SshConfigPath, $SshAlias, $remoteRun) -FailureMessage "Remote Minecraft Velocity command flow failed."

Invoke-Checked -FilePath "scp.exe" -Arguments @("-F", $SshConfigPath, "$SshAlias`:$remoteOutputDir/scenario-result.json", $resultPath) -FailureMessage "Failed to copy remote Minecraft Velocity result."
Invoke-Checked -FilePath "scp.exe" -Arguments @("-F", $SshConfigPath, "$SshAlias`:$remoteOutputDir/transcript.txt", $transcriptPath) -FailureMessage "Failed to copy remote Minecraft Velocity transcript."

Write-LogLine "[$((Get-Date).ToString("o"))] Running Minecraft server command flow directly against kingdom backend."
$remoteServerOutputDir = "$remoteOutputDir-server"
$remoteServerRun = "cd '$RemoteHeadlessDir' && mkdir -p '$remoteServerOutputDir' && MINECRAFT_PROXY_COMMANDS='$ServerCommandList' MINECRAFT_PROXY_COMMAND_DELAY_MS=1000 node '$remoteScriptPath' 127.0.0.1 $KingdomBackendPort 'DirectKingdomBot' '$MinecraftVersion' '$remoteServerOutputDir'"
Invoke-Checked -FilePath "ssh.exe" -Arguments @("-F", $SshConfigPath, $SshAlias, $remoteServerRun) -FailureMessage "Remote Minecraft server command flow failed."

Invoke-Checked -FilePath "scp.exe" -Arguments @("-F", $SshConfigPath, "$SshAlias`:$remoteServerOutputDir/scenario-result.json", $serverResultPath) -FailureMessage "Failed to copy remote Minecraft server result."
Invoke-Checked -FilePath "scp.exe" -Arguments @("-F", $SshConfigPath, "$SshAlias`:$remoteServerOutputDir/transcript.txt", $serverTranscriptPath) -FailureMessage "Failed to copy remote Minecraft server transcript."

Write-LogLine "[$((Get-Date).ToString("o"))] ResultFile=$resultPath"
Write-LogLine "[$((Get-Date).ToString("o"))] TranscriptFile=$transcriptPath"
Write-LogLine "[$((Get-Date).ToString("o"))] ServerResultFile=$serverResultPath"
Write-LogLine "[$((Get-Date).ToString("o"))] ServerTranscriptFile=$serverTranscriptPath"
