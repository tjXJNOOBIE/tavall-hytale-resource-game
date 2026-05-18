param(
    [string]$SshAlias = "novus-remote",
    [string]$SshConfigPath = "C:\Users\TJ\.ssh\config",
    [string]$RemoteControlDir = "/srv/resource-game-control",
    [string]$RemoteMinecraftControlDir = "/srv/resource-game-control",
    [string]$RemoteProxyDir = "/srv/proxy",
    [string]$RemoteBackendDir = "/srv/ffa",
    [string]$RemoteSwitchBackendDir = "/srv/ffa-switch",
    [string]$RemoteKingdomBackendDir = "/srv/mc-kingdom-servers/mc-kingdom-server-1",
    [string]$RemoteCloudAgentDir = "/srv/tavall-cloud-agent",
    [string]$RemoteHytaleDir = "/srv/hytale",
    [int]$ControlPort = 18082,
    [int]$MinecraftControlPort = 18080,
    [int]$FfaPort = 25566,
    [int]$SwitchPort = 25567,
    [int]$KingdomPort = 25568,
    [int]$HytaleBridgePort = 5522,
    [string]$ControlIngressUrl = "tcp://127.0.0.1:18081",
    [string]$InstanceServerMap = "kingdom-1-minecraft-primary=kingdom,kingdom-2-minecraft-primary=ffa",
    [string]$VelocityOwnerUsernames = "ResourceProxyBot"
)

$ErrorActionPreference = "Stop"

function Invoke-Remote {
    param([string]$Command)
    $normalizedCommand = $Command -replace "`r`n", "`n"
    $normalizedCommand | & ssh.exe -F $SshConfigPath $SshAlias bash -s
    if ($LASTEXITCODE -ne 0) {
        throw "Remote command failed."
    }
}

$remoteScript = @"
set -euo pipefail

restart_tmux() {
  session="`$1"
  workdir="`$2"
  command="`$3"
  tmux has-session -t "`$session" 2>/dev/null && tmux kill-session -t "`$session" || true
  mkdir -p "`$workdir/logs"
  tmux new-session -d -s "`$session" "cd '`$workdir' && exec bash -lc '`$command'"
}

stop_port() {
  port="`$1"
  if command -v fuser >/dev/null 2>&1; then
    fuser -k "`$port/tcp" >/dev/null 2>&1 || true
  fi
}

wait_for_port() {
  port="`$1"
  for attempt in `$(seq 1 60); do
    if (: >/dev/tcp/127.0.0.1/"`$port") >/dev/null 2>&1; then
      return 0
    fi
    sleep 1
  done
  echo "port did not open: `$port" >&2
  return 1
}

stop_port $ControlPort
stop_port $MinecraftControlPort
stop_port 25565
stop_port $FfaPort
stop_port $SwitchPort
stop_port $KingdomPort
stop_port $HytaleBridgePort

restart_tmux control '$RemoteControlDir' 'java --enable-preview -Dserver.port=$ControlPort -jar control-server.jar'
restart_tmux minecraft-control '$RemoteMinecraftControlDir' 'java --enable-preview -Dserver.port=$MinecraftControlPort -jar control-server.jar'
wait_for_port $ControlPort
wait_for_port $MinecraftControlPort
restart_tmux minecraft-ffa '$RemoteBackendDir' 'RESOURCE_GAME_MINECRAFT_SERVER_ID=minecraft-backend-ffa RESOURCE_GAME_MINECRAFT_PROXY_ID=velocity-proxy java -Xss1650k -Xmx1536M -jar spigot.jar nogui'
restart_tmux minecraft-switch '$RemoteSwitchBackendDir' 'RESOURCE_GAME_MINECRAFT_SERVER_ID=minecraft-backend-switch RESOURCE_GAME_MINECRAFT_PROXY_ID=velocity-proxy java -Xss1650k -Xmx1024M -jar spigot.jar nogui'
restart_tmux minecraft-kingdom '$RemoteKingdomBackendDir' 'RESOURCE_GAME_MINECRAFT_SERVER_ID=mc-kingdom-server-1 RESOURCE_GAME_MINECRAFT_PROXY_ID=velocity-proxy java -Xmx1536M -jar server.jar nogui'
restart_tmux minecraft-proxy '$RemoteProxyDir' 'RESOURCE_GAME_MINECRAFT_OWNER_USERNAMES=$VelocityOwnerUsernames RESOURCE_GAME_MINECRAFT_SERVER_ID=velocity-proxy RESOURCE_GAME_MINECRAFT_INSTANCE_SERVER_MAP=$InstanceServerMap bash ./start.sh'
wait_for_port $FfaPort
wait_for_port $SwitchPort
wait_for_port $KingdomPort
wait_for_port 25565
if [ -d '$RemoteHytaleDir' ]; then
  restart_tmux hytale '$RemoteHytaleDir' 'RESOURCE_GAME_HYTALE_CONTROL_INGRESS_URL=tcp://127.0.0.1:18081 RESOURCE_GAME_CONTROL_INGRESS_URL=tcp://127.0.0.1:18081 RESOURCE_GAME_HYTALE_SERVER_ID=hytale-single-server ./start.sh'
fi
if [ -f '$RemoteControlDir/control-server.jar' ]; then
  restart_tmux cloud-agent '$RemoteCloudAgentDir' 'TAVALL_CLOUD_CONTROL_PLANE_URL=http://127.0.0.1:$ControlPort TAVALL_CLOUD_AGENT_VERSION=0.1.1-SNAPSHOT java --enable-preview -cp '"'"'$RemoteControlDir/control-server.jar'"'"' org.tavall.control.cloud.CloudAgentApplication'
fi

sleep 2
for session in control minecraft-control minecraft-proxy minecraft-ffa minecraft-switch minecraft-kingdom; do
  if ! tmux has-session -t "`$session" 2>/dev/null; then
    echo "missing tmux session: `$session" >&2
    exit 1
  fi
done

echo 'tmux sessions:'
tmux list-sessions
echo ''
echo 'attach commands:'
echo 'tmux attach -t control'
echo 'tmux attach -t minecraft-control'
echo 'tmux attach -t minecraft-proxy'
echo 'tmux attach -t minecraft-ffa'
echo 'tmux attach -t minecraft-switch'
echo 'tmux attach -t minecraft-kingdom'
echo 'tmux attach -t cloud-agent'
echo 'tmux attach -t hytale'
"@

Invoke-Remote -Command $remoteScript
