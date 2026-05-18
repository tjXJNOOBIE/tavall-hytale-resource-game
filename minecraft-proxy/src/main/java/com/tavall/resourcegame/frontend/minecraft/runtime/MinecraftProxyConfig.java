package org.tavall.minecraft.runtime;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record MinecraftProxyConfig(
        String commandPermission,
        String adminPermission,
        String serverId,
        Map<String, String> instanceServerMappings,
        Set<String> ownerUsernames,
        Set<String> adminUsernames,
        boolean allowConsoleAdmin
) implements IMinecraftProxyConfig, IDependencyInjectableConcrete {
    public static MinecraftProxyConfig fromEnvironment(Map<String, String> environment) {
        return new MinecraftProxyConfig(
                firstNonBlank(environment.get("RESOURCE_GAME_MINECRAFT_COMMAND_PERMISSION"), "tavall.resourcegame.command"),
                firstNonBlank(environment.get("RESOURCE_GAME_MINECRAFT_ADMIN_PERMISSION"), "tavall.resourcegame.admin"),
                firstNonBlank(environment.get("RESOURCE_GAME_MINECRAFT_SERVER_ID"), "velocity-proxy"),
                normalizedMap(environment.get("RESOURCE_GAME_MINECRAFT_INSTANCE_SERVER_MAP")),
                normalizedSet(environment.get("RESOURCE_GAME_MINECRAFT_OWNER_USERNAMES")),
                normalizedSet(environment.get("RESOURCE_GAME_MINECRAFT_ADMIN_USERNAMES")),
                Boolean.parseBoolean(firstNonBlank(environment.get("RESOURCE_GAME_MINECRAFT_CONSOLE_ADMIN"), "true"))
        );
    }

    private static String firstNonBlank(String candidate, String fallback) {
        return candidate == null || candidate.isBlank() ? fallback : candidate;
    }

    private static Set<String> normalizedSet(String csv) {
        if (csv == null || csv.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(entry -> !entry.isBlank())
                .map(entry -> entry.toLowerCase(java.util.Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    private static Map<String, String> normalizedMap(String csv) {
        if (csv == null || csv.isBlank()) {
            return Map.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(entry -> !entry.isBlank())
                .map(entry -> entry.split("=", 2))
                .filter(parts -> parts.length == 2 && !parts[0].isBlank() && !parts[1].isBlank())
                .collect(Collectors.toUnmodifiableMap(parts -> parts[0].trim(), parts -> parts[1].trim(), (left, right) -> right));
    }
}
