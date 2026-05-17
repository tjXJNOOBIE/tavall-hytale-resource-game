package com.tavall.resourcegame.api.internal.permissions;

import java.util.EnumSet;
import java.util.Set;

public enum UniversalPermissionRole {
    MEMBER(10, EnumSet.of(
            UniversalPermission.VIEW_GAME_SUMMARY,
            UniversalPermission.EXECUTE_USER_COMMAND
    )),
    MODERATOR(50, EnumSet.of(
            UniversalPermission.VIEW_GAME_SUMMARY,
            UniversalPermission.EXECUTE_USER_COMMAND,
            UniversalPermission.EXECUTE_DEBUG_COMMAND,
            UniversalPermission.VIEW_AUDIT_LOG
    )),
    ADMIN(80, EnumSet.complementOf(EnumSet.of(UniversalPermission.MANAGE_PERMISSIONS))),
    OWNER(100, EnumSet.allOf(UniversalPermission.class)),
    SYSTEM(1000, EnumSet.allOf(UniversalPermission.class));

    private final int powerLevel;
    private final Set<UniversalPermission> permissions;

    UniversalPermissionRole(int powerLevel, Set<UniversalPermission> permissions) {
        this.powerLevel = powerLevel;
        this.permissions = Set.copyOf(permissions);
    }

    public int powerLevel() {
        return powerLevel;
    }

    public boolean hasPermission(UniversalPermission permission) {
        return permissions.contains(permission);
    }

    public Set<UniversalPermission> permissions() {
        return permissions;
    }
}
