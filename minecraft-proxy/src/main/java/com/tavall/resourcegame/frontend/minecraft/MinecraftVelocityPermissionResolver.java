package com.tavall.resourcegame.frontend.minecraft;

import com.tavall.resourcegame.api.internal.frontend.ResourceGameFrontendPlatform;
import com.tavall.resourcegame.api.internal.permissions.UniversalPermissionRole;
import com.tavall.resourcegame.api.internal.permissions.UniversalPermissionSubject;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.util.Set;

public final class MinecraftVelocityPermissionResolver implements IMinecraftVelocityPermissionResolver, IMinecraftFrontendDomain, IDependencyInjectableConcrete {
    @Override
    public UniversalPermissionSubject resolveSubject(MinecraftVelocityCommandSource source) {
        return new UniversalPermissionSubject(
                ResourceGameFrontendPlatform.MINECRAFT,
                source.platformAccountId(),
                source.platformDisplayName(),
                resolveRole(source),
                Set.of()
        );
    }

    @Override
    public UniversalPermissionRole resolveRole(MinecraftVelocityCommandSource source) {
        String displayName = source.platformDisplayName().toLowerCase(java.util.Locale.ROOT);
        if (getMinecraftProxyConfig().ownerUsernames().contains(displayName)) {
            return UniversalPermissionRole.OWNER;
        }
        if ((source.sourceType().equals("console") && getMinecraftProxyConfig().allowConsoleAdmin())
                || source.hasPermission(getMinecraftProxyConfig().adminPermission())
                || getMinecraftProxyConfig().adminUsernames().contains(displayName)) {
            return UniversalPermissionRole.ADMIN;
        }
        if (source.hasPermission(getMinecraftProxyConfig().commandPermission())) {
            return UniversalPermissionRole.MEMBER;
        }
        return UniversalPermissionRole.MEMBER;
    }
}
