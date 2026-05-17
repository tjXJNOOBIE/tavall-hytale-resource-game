package com.tavall.resourcegame.shared.permissions;

import com.tavall.resourcegame.shared.frontend.ResourceGameFrontendPlatform;

import java.util.Objects;
import java.util.Set;

public record UniversalPermissionSubject(
        ResourceGameFrontendPlatform platform,
        String platformAccountId,
        String displayName,
        UniversalPermissionRole role,
        Set<UniversalPermission> explicitPermissions
) {
    public UniversalPermissionSubject {
        Objects.requireNonNull(platform, "platform");
        Objects.requireNonNull(platformAccountId, "platformAccountId");
        displayName = displayName == null ? platformAccountId : displayName;
        role = role == null ? UniversalPermissionRole.MEMBER : role;
        explicitPermissions = explicitPermissions == null ? Set.of() : Set.copyOf(explicitPermissions);
    }

    public boolean hasPermission(UniversalPermission permission) {
        return role.hasPermission(permission) || explicitPermissions.contains(permission);
    }
}
