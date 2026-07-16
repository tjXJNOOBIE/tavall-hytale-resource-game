param(
    [ValidateSet("DefaultProfile", "AutomationProfile", "Probe")]
    [string]$Mode = "Probe",
    [int]$Port = 9222,
    [string]$Url = "https://discord.com/developers/applications",
    [string]$AutomationProfilePath = "$env:LOCALAPPDATA\Tavall\ResourceGame\ChromeAutomationProfile"
)

$ErrorActionPreference = "Stop"

function Resolve-ChromePath {
    $candidatePaths = @(
        "${env:ProgramFiles(x86)}\Google\Chrome\Application\chrome.exe",
        "$env:ProgramFiles\Google\Chrome\Application\chrome.exe"
    )

    foreach ($candidatePath in $candidatePaths) {
        if (Test-Path -LiteralPath $candidatePath) {
            return $candidatePath
        }
    }

    throw "Chrome executable was not found."
}

function Test-ChromeDevToolsEndpoint {
    param([int]$EndpointPort)

    try {
        $version = Invoke-RestMethod -Uri "http://127.0.0.1:$EndpointPort/json/version" -TimeoutSec 2
        [pscustomobject]@{
            Listening = $true
            Port = $EndpointPort
            Browser = $version.Browser
            WebSocketDebuggerUrl = $version.webSocketDebuggerUrl
        }
    } catch {
        [pscustomobject]@{
            Listening = $false
            Port = $EndpointPort
            Browser = $null
            WebSocketDebuggerUrl = $null
        }
    }
}

function Start-ChromeWithArguments {
    param(
        [string]$ChromePath,
        [string[]]$ChromeArguments
    )

    Start-Process -FilePath $ChromePath -ArgumentList $ChromeArguments
}

$chromePath = Resolve-ChromePath

if ($Mode -eq "Probe") {
    $probeResult = Test-ChromeDevToolsEndpoint -EndpointPort $Port
    $probeResult | Format-List
    if (-not $probeResult.Listening) {
        Write-Host "No Chrome DevTools endpoint is available at http://127.0.0.1:$Port/json/version."
    }
    return
}

if ($Mode -eq "DefaultProfile") {
    Start-ChromeWithArguments -ChromePath $chromePath -ChromeArguments @(
        "--remote-debugging-port=$Port",
        "--remote-debugging-address=127.0.0.1",
        "--remote-allow-origins=*",
        "--restore-last-session",
        $Url
    )

    Start-Sleep -Seconds 3
    $probeResult = Test-ChromeDevToolsEndpoint -EndpointPort $Port
    $probeResult | Format-List

    if (-not $probeResult.Listening) {
        Write-Host "Chrome did not expose DevTools for the default signed-in profile."
        Write-Host "Chrome 136+ ignores remote debugging flags for the default user-data-dir."
        Write-Host "Use -Mode AutomationProfile with the same Chrome executable, then sign in once in that profile."
    }
    return
}

if ($Mode -eq "AutomationProfile") {
    New-Item -ItemType Directory -Force -Path $AutomationProfilePath | Out-Null

    Start-ChromeWithArguments -ChromePath $chromePath -ChromeArguments @(
        "--user-data-dir=$AutomationProfilePath",
        "--remote-debugging-port=$Port",
        "--remote-debugging-address=127.0.0.1",
        "--remote-allow-origins=*",
        "--no-first-run",
        "--no-default-browser-check",
        $Url
    )

    $deadline = (Get-Date).AddSeconds(20)
    do {
        Start-Sleep -Milliseconds 500
        $probeResult = Test-ChromeDevToolsEndpoint -EndpointPort $Port
        if ($probeResult.Listening) {
            $probeResult | Format-List
            return
        }
    } while ((Get-Date) -lt $deadline)

    throw "Chrome automation profile started, but DevTools did not become available on port $Port."
}
