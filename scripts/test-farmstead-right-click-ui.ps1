param(
    [string]$ServerRoot = "C:\Users\TJ\Documents\HyTaleDevServer",
    [string]$ServerHost = "127.0.0.1",
    [int]$Port = 5520,
    [string]$Username = "FarmsteadBot",
    [string]$StableUuid = "",
    [string]$AuthDomain = "auth.sanasol.ws",
    [string]$IdentityToken = "",
    [string]$SessionToken = "",
    [string]$AuthPassword = "",
    [string]$AuthScopes = "",
    [string]$OutputDir = "",
    [switch]$KeepServerRunning
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
if ([string]::IsNullOrWhiteSpace($OutputDir)) {
    $OutputDir = Join-Path $repoRoot "build\test-results\farmstead-right-click-ui"
}
$botRoot = Join-Path $repoRoot "..\..\tavall-java-game-tools\hytale-bots"
$botClientPath = Join-Path $botRoot "packages\client\dist\index.js"
$scenarioScript = Join-Path $repoRoot "scripts\farmstead-right-click-ui-flow.mjs"
$serverJarPath = Join-Path $ServerRoot "HytaleServer.jar"
$runLog = Join-Path $OutputDir "farmstead-right-click-ui-run.log"
$summaryPath = Join-Path $OutputDir "summary.txt"
$childPowerShell = if (Get-Command pwsh.exe -ErrorAction SilentlyContinue) { "pwsh.exe" } else { "powershell.exe" }

New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null

function Write-LogLine {
    param([string]$Message)
    $line = "[{0}] {1}" -f (Get-Date).ToString("o"), $Message
    Add-Content -Path $runLog -Value $line -Encoding utf8
    Write-Host $line
}

function Invoke-Step {
    param(
        [string]$Name,
        [scriptblock]$ScriptBlock
    )
    Write-LogLine "START $Name"
    & $ScriptBlock
    Write-LogLine "PASS $Name"
}

function Invoke-LoggedProcess {
    param(
        [string]$FilePath,
        [string[]]$Arguments
    )

    $stdoutPath = [System.IO.Path]::GetTempFileName()
    $stderrPath = [System.IO.Path]::GetTempFileName()
    try {
        $process = Start-Process -FilePath $FilePath `
            -ArgumentList $Arguments `
            -Wait `
            -NoNewWindow `
            -PassThru `
            -RedirectStandardOutput $stdoutPath `
            -RedirectStandardError $stderrPath

        foreach ($path in @($stdoutPath, $stderrPath)) {
            if (Test-Path -LiteralPath $path) {
                Get-Content -Path $path | ForEach-Object {
                    Add-Content -Path $runLog -Value $_ -Encoding utf8
                    Write-Host $_
                }
            }
        }

        return [int]$process.ExitCode
    } finally {
        foreach ($path in @($stdoutPath, $stderrPath)) {
            if (Test-Path -LiteralPath $path) {
                Remove-Item -LiteralPath $path -Force
            }
        }
    }
}

function Test-UdpPortOpen {
    param([int]$TargetPort)
    $listener = Get-NetUDPEndpoint -ErrorAction SilentlyContinue | Where-Object { $_.LocalPort -eq $TargetPort }
    return $null -ne $listener
}

function Stop-LocalHytaleServer {
    param([int]$TargetPort)
    $serverRootPattern = [Regex]::Escape($ServerRoot)
    $serverProcesses = Get-CimInstance Win32_Process -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -match "^java(\.exe)?$" -and $_.CommandLine -match "HytaleServer\.jar" -and $_.CommandLine -match $serverRootPattern }
    $launcherProcesses = Get-CimInstance Win32_Process -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -eq "cmd.exe" -and $_.CommandLine -match $serverRootPattern -and $_.CommandLine -match "start\.bat" }
    $listenerProcesses = Get-NetUDPEndpoint -ErrorAction SilentlyContinue |
        Where-Object { $_.LocalPort -eq $TargetPort } |
        Select-Object -ExpandProperty OwningProcess -Unique

    $processIds = @(
        $serverProcesses | Select-Object -ExpandProperty ProcessId
        $launcherProcesses | Select-Object -ExpandProperty ProcessId
        $listenerProcesses
    ) | Where-Object { $_ } | Sort-Object -Unique

    foreach ($processId in $processIds) {
        try {
            & taskkill.exe /F /T /PID $processId | Out-Null
            Write-LogLine "Stopped local Hytale process tree $processId."
        } catch {
            Write-LogLine "Failed to stop local Hytale process tree ${processId}: $($_.Exception.Message)"
        }
    }
}

if (-not (Test-Path -LiteralPath $botRoot -PathType Container)) {
    throw "Bot harness root not found: $botRoot"
}
if (-not (Test-Path -LiteralPath $scenarioScript -PathType Leaf)) {
    throw "Scenario script not found: $scenarioScript"
}
if (-not (Test-Path -LiteralPath $serverJarPath -PathType Leaf)) {
    throw "Hytale server jar not found: $serverJarPath"
}

$startedAt = (Get-Date).ToString("o")
$exitCode = 1
$previousBotClient = $env:HYTALE_BOT_CLIENT
if ([string]::IsNullOrWhiteSpace($StableUuid)) {
    $StableUuid = [guid]::NewGuid().ToString()
}

try {
    Invoke-Step "build bot harness" {
        Push-Location $botRoot
        try {
            & npm.cmd run build *>&1 | Tee-Object -FilePath $runLog -Append
            if ($LASTEXITCODE -ne 0) {
                throw "npm run build failed with exit code $LASTEXITCODE"
            }
        } finally {
            Pop-Location
        }
    }
    if (-not (Test-Path -LiteralPath $botClientPath -PathType Leaf)) {
        throw "Built bot client not found: $botClientPath"
    }
    $env:HYTALE_BOT_CLIENT = $botClientPath

    Invoke-Step "deploy plugin jar" {
        $code = Invoke-LoggedProcess -FilePath $childPowerShell -Arguments @(
            "-NoProfile",
            "-ExecutionPolicy",
            "Bypass",
            "-File",
            (Join-Path $PSScriptRoot "deploy-local-plugin.ps1"),
            "-ServerRoot",
            $ServerRoot,
            "-Build"
        )
        if ($code -ne 0) {
            throw "deploy-local-plugin failed with exit code $code"
        }
    }

    Invoke-Step "sync hot assets" {
        $code = Invoke-LoggedProcess -FilePath $childPowerShell -Arguments @(
            "-NoProfile",
            "-ExecutionPolicy",
            "Bypass",
            "-File",
            (Join-Path $PSScriptRoot "sync-local-hot-assets.ps1"),
            "-ServerRoot",
            $ServerRoot,
            "-ProcessResources"
        )
        if ($code -ne 0) {
            throw "sync-local-hot-assets failed with exit code $code"
        }
    }

    Invoke-Step "restart local Hytale test server" {
        & (Join-Path $PSScriptRoot "restart-local-dev-server.ps1") `
            -ServerRoot $ServerRoot `
            -DeployPlugin:$false `
            -NoExitOnReady `
            -Port $Port *>&1 | Tee-Object -FilePath $runLog -Append
        if (-not (Test-UdpPortOpen -TargetPort $Port)) {
            throw "Local Hytale server did not open UDP port $Port"
        }
    }

    Invoke-Step "run Farmstead Steward right-click bot scenario" {
        $scenarioArguments = @(
            "-NoProfile",
            "-ExecutionPolicy",
            "Bypass",
            "-File",
            (Join-Path $PSScriptRoot "run-local-gameplay-flow.ps1"),
            "-ScenarioScript",
            $scenarioScript,
            "-ScenarioName",
            "farmstead-right-click-ui",
            "-ServerHost",
            $ServerHost,
            "-Port",
            ([string]$Port),
            "-Username",
            $Username,
            "-StableUuid",
            $StableUuid,
            "-LogDir",
            $OutputDir,
            "-ServerJarPath",
            $serverJarPath
        )
        if (-not [string]::IsNullOrWhiteSpace($AuthDomain)) {
            $scenarioArguments += @("-AuthDomain", $AuthDomain)
        }
        if (-not [string]::IsNullOrWhiteSpace($IdentityToken)) {
            $scenarioArguments += @("-IdentityToken", $IdentityToken)
        }
        if (-not [string]::IsNullOrWhiteSpace($SessionToken)) {
            $scenarioArguments += @("-SessionToken", $SessionToken)
        }
        if (-not [string]::IsNullOrWhiteSpace($AuthPassword)) {
            $scenarioArguments += @("-AuthPassword", $AuthPassword)
        }
        if (-not [string]::IsNullOrWhiteSpace($AuthScopes)) {
            $scenarioArguments += @("-AuthScopes", $AuthScopes)
        }
        if ($env:RESOURCE_GAME_BOT_TRACE -match "^(full|trace|debug)$") {
            $scenarioArguments += @("-KeepTranscript")
        }
        $script:exitCode = Invoke-LoggedProcess -FilePath $childPowerShell -Arguments $scenarioArguments
        if ($script:exitCode -ne 0) {
            throw "Farmstead right-click UI scenario failed with exit code $script:exitCode"
        }
    }
} catch {
    Write-LogLine "FAIL $($_.Exception.Message)"
    $exitCode = if ($exitCode -eq 0) { 1 } else { $exitCode }
} finally {
    $env:HYTALE_BOT_CLIENT = $previousBotClient
    if (-not $KeepServerRunning) {
        Stop-LocalHytaleServer -TargetPort $Port
    }
}

$completedAt = (Get-Date).ToString("o")
$success = $exitCode -eq 0
$summary = @(
    "startedAt=$startedAt",
    "completedAt=$completedAt",
    "success=$success",
    "host=$ServerHost",
    "port=$Port",
    "username=$Username",
    "stableUuid=$StableUuid",
    "authDomain=$AuthDomain",
    "outputDir=$OutputDir",
    "runLog=$runLog"
)
Set-Content -Path $summaryPath -Value $summary -Encoding utf8
if ($success) {
    Write-LogLine "PASS FarmsteadStewardRightClickMenuBotTest"
} else {
    Write-LogLine "FAIL FarmsteadStewardRightClickMenuBotTest"
}
Write-LogLine "SummaryFile=$summaryPath"
exit $exitCode
