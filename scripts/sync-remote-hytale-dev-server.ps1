param(
    [string]$SshAlias = "novus-remote",
    [string]$SshConfigPath = "C:\Users\TJ\.ssh\config",
    [string]$LocalDevServerDir = "",
    [string]$RemoteServerRoot = "/srv/hytale/HytaleDevServer",
    [string]$LogDir = "",
    [switch]$SkipCopy
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
if ([string]::IsNullOrWhiteSpace($LocalDevServerDir)) {
    $LocalDevServerDir = Join-Path $env:USERPROFILE "Documents\HytaleDevServer"
}
if ([string]::IsNullOrWhiteSpace($LogDir)) {
    $LogDir = Join-Path $repoRoot "bot-logs"
}
New-Item -ItemType Directory -Force -Path $LogDir | Out-Null

if (-not (Test-Path -LiteralPath $LocalDevServerDir)) {
    throw "Local HytaleDevServer folder not found: $LocalDevServerDir"
}

$requiredFiles = @("HytaleServer.jar", "Assets.zip", "config.json", "permissions.json")
foreach ($requiredFile in $requiredFiles) {
    $requiredPath = Join-Path $LocalDevServerDir $requiredFile
    if (-not (Test-Path -LiteralPath $requiredPath)) {
        throw "Local HytaleDevServer is missing required file: $requiredPath"
    }
}

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$archivePath = Join-Path $LogDir ("hytale-dev-server-{0}.tar" -f $timestamp)
$remoteArchivePath = "/tmp/hytale-dev-server-{0}.tar" -f $timestamp

function Invoke-CheckedProcess {
    param(
        [string]$FilePath,
        [string[]]$Arguments,
        [string]$FailureMessage
    )

    & $FilePath @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw $FailureMessage
    }
}

try {
    if (-not $SkipCopy) {
        Write-Host "Packing exact local HytaleDevServer folder: $LocalDevServerDir"
        Invoke-CheckedProcess -FilePath "tar.exe" -Arguments @(
            "-C", $LocalDevServerDir,
            "-cf", $archivePath,
            "."
        ) -FailureMessage "Failed to archive local HytaleDevServer folder."

        Write-Host "Copying HytaleDevServer archive to ${SshAlias}:$remoteArchivePath"
        Invoke-CheckedProcess -FilePath "scp.exe" -Arguments @(
            "-F", $SshConfigPath,
            $archivePath,
            ("{0}:{1}" -f $SshAlias, $remoteArchivePath)
        ) -FailureMessage "Failed to copy HytaleDevServer archive to remote."
    }

    $remoteScript = @'
set -euo pipefail
REMOTE_ROOT='__REMOTE_ROOT__'
REMOTE_ARCHIVE='__REMOTE_ARCHIVE__'

if [ -f "$REMOTE_ARCHIVE" ]; then
  rm -rf "$REMOTE_ROOT"
  mkdir -p "$REMOTE_ROOT"
  tar -C "$REMOTE_ROOT" -xf "$REMOTE_ARCHIVE"
  rm -f "$REMOTE_ARCHIVE"
fi

cd "$REMOTE_ROOT"
mkdir -p mods logs universe _bot
ln -sfn . Server

cat > start.sh <<'EOF'
#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

JAVA_CMD="${JAVA_CMD:-java}"
SERVER_JAR="${SERVER_JAR:-./HytaleServer.jar}"
ASSETS_PATH="${ASSETS_PATH:-./Assets.zip}"
AGENT_JAR="${AGENT_JAR:-./dualauth-agent.jar}"
SERVER_NAME="${SERVER_NAME:-My Hytale Server}"
AUTH_SERVER="${AUTH_SERVER:-https://${HYTALE_AUTH_DOMAIN:-auth.sanasol.ws}}"
JVM_OPTS="${JVM_OPTS:-}"
SESSION_TOKEN="${HYTALE_SESSION_TOKEN:-}"
IDENTITY_TOKEN="${HYTALE_IDENTITY_TOKEN:-}"

JAVA_ARGS=()
if [ -n "$JVM_OPTS" ]; then
  # shellcheck disable=SC2206
  JAVA_ARGS=($JVM_OPTS)
fi

if [ "${HYTALE_REMOTE_USE_DUALAUTH_AGENT:-true}" = "true" ] && [ -f "$AGENT_JAR" ]; then
  JAVA_ARGS+=("-javaagent:$AGENT_JAR")
fi

if [ -z "$SESSION_TOKEN" ] || [ -z "$IDENTITY_TOKEN" ]; then
  if [ -f .server-id ]; then
    SERVER_ID="$(tr -d '\r\n' < .server-id)"
  else
    SERVER_ID="$(cat /proc/sys/kernel/random/uuid)"
    printf '%s\n' "$SERVER_ID" > .server-id
  fi

  TEMP_RESPONSE="$(mktemp)"
  cleanup() {
    rm -f "$TEMP_RESPONSE"
  }
  trap cleanup EXIT

  curl -fsS -X POST "$AUTH_SERVER/server/auto-auth" \
    -H "Content-Type: application/json" \
    -d "{\"server_id\":\"$SERVER_ID\",\"server_name\":\"$SERVER_NAME\"}" \
    --connect-timeout 10 \
    --max-time 30 \
    -o "$TEMP_RESPONSE"

  SESSION_TOKEN="$(node -e "const fs=require('fs'); const j=JSON.parse(fs.readFileSync(process.argv[1],'utf8')); process.stdout.write(j.sessionToken || '')" "$TEMP_RESPONSE")"
  IDENTITY_TOKEN="$(node -e "const fs=require('fs'); const j=JSON.parse(fs.readFileSync(process.argv[1],'utf8')); process.stdout.write(j.identityToken || '')" "$TEMP_RESPONSE")"
fi

if [ -z "$SESSION_TOKEN" ] || [ -z "$IDENTITY_TOKEN" ]; then
  echo "Unable to obtain Hytale server auth tokens." >&2
  exit 1
fi

exec "$JAVA_CMD" "${JAVA_ARGS[@]}" -jar "$SERVER_JAR" --assets "$ASSETS_PATH" --disable-sentry "$@" --session-token "$SESSION_TOKEN" --identity-token "$IDENTITY_TOKEN"
EOF
chmod +x start.sh

test -f HytaleServer.jar
test -f Assets.zip
test -d mods
test -L Server
echo "REMOTE_HYTALE_DEV_SERVER_READY=$REMOTE_ROOT"
'@
    $remoteScript = $remoteScript.Replace("__REMOTE_ROOT__", $RemoteServerRoot.Replace("'", "'\''"))
    $remoteScript = $remoteScript.Replace("__REMOTE_ARCHIVE__", $remoteArchivePath.Replace("'", "'\''"))

    Write-Host "Preparing remote HytaleDevServer runtime shims."
    $processInfo = New-Object System.Diagnostics.ProcessStartInfo
    $processInfo.FileName = "ssh.exe"
    $processInfo.Arguments = "-F `"$SshConfigPath`" $SshAlias `"bash -s`""
    $processInfo.RedirectStandardInput = $true
    $processInfo.RedirectStandardOutput = $true
    $processInfo.RedirectStandardError = $true
    $processInfo.UseShellExecute = $false
    $processInfo.CreateNoWindow = $true
    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $processInfo
    $null = $process.Start()
    $process.StandardInput.Write($remoteScript)
    $process.StandardInput.Close()
    $stdout = $process.StandardOutput.ReadToEnd()
    $stderr = $process.StandardError.ReadToEnd()
    $process.WaitForExit()
    if (-not [string]::IsNullOrWhiteSpace($stdout)) {
        Write-Host $stdout.Trim()
    }
    if (-not [string]::IsNullOrWhiteSpace($stderr)) {
        Write-Host $stderr.Trim()
    }
    if ($process.ExitCode -ne 0) {
        throw "Failed to prepare remote HytaleDevServer at $RemoteServerRoot"
    }
} finally {
    if (Test-Path -LiteralPath $archivePath) {
        Remove-Item -LiteralPath $archivePath -Force
    }
}
