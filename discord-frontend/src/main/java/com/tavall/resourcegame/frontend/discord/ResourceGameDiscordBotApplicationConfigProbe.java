package com.tavall.resourcegame.frontend.discord;

record ResourceGameDiscordBotApplicationConfigProbe(
        boolean hasToken,
        String guildId,
        String controlIngressUrl,
        DiscordPermissionMapping permissionMapping
) {
}
