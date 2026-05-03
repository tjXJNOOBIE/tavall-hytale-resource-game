param(
    [int]$Port = 18082,
    [string]$AccountId = "discord-live-local",
    [string]$GuildId = "project-novus-local"
)

$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$jar = Join-Path $root "target\tavall-hytale-resource-game.jar"
if (!(Test-Path $jar)) {
    throw "Control server jar not found: $jar"
}

$logDirectory = Join-Path $root ".codex-temp"
New-Item -ItemType Directory -Force -Path $logDirectory | Out-Null
$outLog = Join-Path $logDirectory "discord-control-live-check.out.log"
$errLog = Join-Path $logDirectory "discord-control-live-check.err.log"

$process = Start-Process -FilePath "java" -ArgumentList @(
    "-cp",
    $jar,
    "com.tavall.hytale.resourcegame.controlserver.web.ControlServerApplication",
    "--server.port=$Port"
) -RedirectStandardOutput $outLog -RedirectStandardError $errLog -WindowStyle Hidden -PassThru

try {
    $ready = $false
    for ($attempt = 0; $attempt -lt 40; $attempt++) {
        Start-Sleep -Milliseconds 500
        try {
            Invoke-WebRequest -Uri "http://127.0.0.1:$Port/control" -UseBasicParsing -TimeoutSec 2 | Out-Null
            $ready = $true
            break
        } catch {
        }
    }
    if (!$ready) {
        throw "Control server did not become ready on port $Port"
    }

    $body = @{
        platform = "DISCORD"
        surface = "COMMAND"
        platformAccountId = $AccountId
        platformDisplayName = "Discord Local"
        rawInput = "/kingdom ui discord-live-verify"
        actionId = $null
        arguments = @{}
        correlationId = "corr-local-http-discord"
        sourceMetadata = @{
            guildId = $GuildId
            channelId = "local"
        }
    } | ConvertTo-Json -Depth 8

    Invoke-RestMethod -Uri "http://127.0.0.1:$Port/api/frontend/commands" -Method Post -ContentType "application/json" -Body $body | ConvertTo-Json -Depth 8
} finally {
    if ($process -and !$process.HasExited) {
        Stop-Process -Id $process.Id -Force
    }
}
