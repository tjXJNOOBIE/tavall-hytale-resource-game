package org.tavall.minecraft.permissions;

import org.tavall.api.minecraft.frontend.ResourceGameFrontendPlatform;
import org.tavall.api.minecraft.permissions.UniversalPermissionRole;
import org.tavall.api.minecraft.permissions.UniversalPermissionSubject;
import org.tavall.minecraft.bridge.IMinecraftFrontendBridgeDependencyAccess;
import org.tavall.minecraft.commands.source.MinecraftVelocityCommandSource;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.util.Set;

public final class MinecraftVelocityPermissionResolver implements IMinecraftVelocityPermissionResolver, IMinecraftFrontendBridgeDependencyAccess, IDependencyInjectableConcrete {
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
