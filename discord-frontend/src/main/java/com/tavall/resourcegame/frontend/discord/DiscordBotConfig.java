package com.tavall.resourcegame.frontend.discord;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record DiscordBotConfig(
        String botToken,
        String guildId,
        String controlIngressUrl,
        DiscordPermissionMapping permissionMapping
) implements IDiscordBotConfig, IDependencyInjectableConcrete {
    public static DiscordBotConfig fromEnvironment(Map<String, String> environment) {
        String botToken = environment.getOrDefault("RESOURCE_GAME_DISCORD_BOT_TOKEN", environment.getOrDefault("DISCORD_BOT_TOKEN", ""));
        String guildId = environment.getOrDefault("RESOURCE_GAME_DISCORD_GUILD_ID", environment.getOrDefault("DISCORD_GUILD_ID", ""));
        String controlIngressUrl = environment.getOrDefault("RESOURCE_GAME_CONTROL_INGRESS_URL", "tcp://127.0.0.1:18081");
        DiscordPermissionMapping mapping = new DiscordPermissionMapping(
                split(environment.get("RESOURCE_GAME_DISCORD_OWNER_USER_IDS")),
                split(environment.get("RESOURCE_GAME_DISCORD_ADMIN_ROLE_IDS")),
                split(environment.get("RESOURCE_GAME_DISCORD_ADMIN_ROLE_NAMES")),
                split(environment.get("RESOURCE_GAME_DISCORD_MODERATOR_ROLE_IDS")),
                split(environment.get("RESOURCE_GAME_DISCORD_MODERATOR_ROLE_NAMES"))
        );
        return new DiscordBotConfig(botToken, guildId, controlIngressUrl, mapping);
    }

    public boolean hasToken() {
        return botToken != null && !botToken.isBlank();
    }

    public boolean hasGuildId() {
        return guildId != null && !guildId.isBlank();
    }

    public boolean hasControlIngressUrl() {
        return controlIngressUrl != null && !controlIngressUrl.isBlank();
    }

    private static Set<String> split(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(token -> !token.isBlank())
                .collect(Collectors.toUnmodifiableSet());
    }
}
