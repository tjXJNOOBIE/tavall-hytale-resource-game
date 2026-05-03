param(
    [string]$ClientId = $env:RESOURCE_GAME_DISCORD_CLIENT_ID,
    [string]$BotToken = $env:RESOURCE_GAME_DISCORD_BOT_TOKEN,
    [string]$GuildId = $env:RESOURCE_GAME_DISCORD_GUILD_ID,
    [int64]$Permissions = 84992,
    [switch]$PrintOnly
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($ClientId)) {
    if ([string]::IsNullOrWhiteSpace($BotToken)) {
        throw "Set RESOURCE_GAME_DISCORD_CLIENT_ID or RESOURCE_GAME_DISCORD_BOT_TOKEN before opening the Discord install URL."
    }
    $headers = @{ Authorization = "Bot $BotToken" }
    $application = Invoke-RestMethod -Uri "https://discord.com/api/v10/oauth2/applications/@me" -Headers $headers -Method Get
    $ClientId = $application.id
}

if ([string]::IsNullOrWhiteSpace($ClientId)) {
    throw "Discord application client id could not be resolved."
}

$scope = [uri]::EscapeDataString("bot applications.commands")
$url = "https://discord.com/oauth2/authorize?client_id=$ClientId&permissions=$Permissions&scope=$scope"

if (![string]::IsNullOrWhiteSpace($GuildId)) {
    $url = "$url&guild_id=$GuildId&disable_guild_select=true"
}

Write-Output $url
if (!$PrintOnly) {
    Start-Process $url
}
