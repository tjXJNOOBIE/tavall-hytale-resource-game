package com.tavall.hytale.resourcegame.frontend.minecraft;

import com.tavall.hytale.resourcegame.shared.frontend.ResourceGameFrontendPlatform;
import com.tavall.hytale.resourcegame.shared.permissions.UniversalPermissionRole;
import com.tavall.hytale.resourcegame.shared.permissions.UniversalPermissionSubject;

import java.util.Set;

public final class MinecraftVelocityPermissionResolver {
    private final String commandPermission;
    private final String adminPermission;
    private final Set<String> ownerUsernames;
    private final Set<String> adminUsernames;
    private final boolean allowConsoleAdmin;

    public MinecraftVelocityPermissionResolver(String commandPermission, String adminPermission) {
        this(commandPermission, adminPermission, Set.of(), Set.of(), true);
    }

    public MinecraftVelocityPermissionResolver(
            String commandPermission,
            String adminPermission,
            Set<String> ownerUsernames,
            Set<String> adminUsernames,
            boolean allowConsoleAdmin
    ) {
        this.commandPermission = commandPermission;
        this.adminPermission = adminPermission;
        this.ownerUsernames = ownerUsernames == null ? Set.of() : Set.copyOf(ownerUsernames);
        this.adminUsernames = adminUsernames == null ? Set.of() : Set.copyOf(adminUsernames);
        this.allowConsoleAdmin = allowConsoleAdmin;
    }

    public UniversalPermissionSubject resolveSubject(MinecraftVelocityCommandSource source) {
        return new UniversalPermissionSubject(
                ResourceGameFrontendPlatform.MINECRAFT,
                source.platformAccountId(),
                source.platformDisplayName(),
                resolveRole(source),
                Set.of()
        );
    }

    public UniversalPermissionRole resolveRole(MinecraftVelocityCommandSource source) {
        String displayName = source.platformDisplayName().toLowerCase(java.util.Locale.ROOT);
        if (ownerUsernames.contains(displayName)) {
            return UniversalPermissionRole.OWNER;
        }
        if ((source.sourceType().equals("console") && allowConsoleAdmin) || source.hasPermission(adminPermission) || adminUsernames.contains(displayName)) {
            return UniversalPermissionRole.ADMIN;
        }
        if (source.hasPermission(commandPermission)) {
            return UniversalPermissionRole.MEMBER;
        }
        return UniversalPermissionRole.MEMBER;
    }
}
