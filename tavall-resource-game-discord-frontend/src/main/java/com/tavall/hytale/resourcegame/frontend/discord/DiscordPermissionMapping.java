package com.tavall.hytale.resourcegame.frontend.discord;

import java.util.Set;

public record DiscordPermissionMapping(
        Set<String> ownerUserIds,
        Set<String> adminRoleIds,
        Set<String> adminRoleNames,
        Set<String> moderatorRoleIds,
        Set<String> moderatorRoleNames
) {
    public DiscordPermissionMapping {
        ownerUserIds = normalize(ownerUserIds);
        adminRoleIds = normalize(adminRoleIds);
        adminRoleNames = normalize(adminRoleNames);
        moderatorRoleIds = normalize(moderatorRoleIds);
        moderatorRoleNames = normalize(moderatorRoleNames);
    }

    public static DiscordPermissionMapping empty() {
        return new DiscordPermissionMapping(Set.of(), Set.of(), Set.of(), Set.of(), Set.of());
    }

    private static Set<String> normalize(Set<String> values) {
        if (values == null || values.isEmpty()) {
            return Set.of();
        }
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }
}
