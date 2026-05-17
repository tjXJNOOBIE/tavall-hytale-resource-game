package com.tavall.resourcegame.frontend.discord;

import java.util.Set;

public record DiscordPermissionContext(
        String userId,
        String displayName,
        boolean guildOwner,
        Set<String> roleIds,
        Set<String> roleNames
) {
    public DiscordPermissionContext {
        displayName = displayName == null || displayName.isBlank() ? userId : displayName;
        roleIds = roleIds == null ? Set.of() : Set.copyOf(roleIds);
        roleNames = roleNames == null ? Set.of() : Set.copyOf(roleNames);
    }
}
