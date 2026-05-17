package com.tavall.resourcegame.frontend.discord;

import com.tavall.resourcegame.shared.frontend.ResourceGameFrontendPlatform;
import com.tavall.resourcegame.shared.permissions.UniversalPermissionRole;
import com.tavall.resourcegame.shared.permissions.UniversalPermissionSubject;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.util.Set;

public final class DiscordPermissionResolver implements IDiscordPermissionResolver, IDiscordFrontendDomain, IDependencyInjectableConcrete {
    @Override
    public UniversalPermissionSubject resolveSubject(DiscordPermissionContext context) {
        UniversalPermissionRole role = resolveRole(context);
        return new UniversalPermissionSubject(
                ResourceGameFrontendPlatform.DISCORD,
                context.userId(),
                context.displayName(),
                role,
                Set.of()
        );
    }

    @Override
    public UniversalPermissionRole resolveRole(DiscordPermissionContext context) {
        if (context.guildOwner() || permissionMapping().ownerUserIds().contains(context.userId())) {
            return UniversalPermissionRole.OWNER;
        }
        if (matchesAny(context.roleIds(), permissionMapping().adminRoleIds()) || matchesAny(context.roleNames(), permissionMapping().adminRoleNames())) {
            return UniversalPermissionRole.ADMIN;
        }
        if (matchesAny(context.roleIds(), permissionMapping().moderatorRoleIds()) || matchesAny(context.roleNames(), permissionMapping().moderatorRoleNames())) {
            return UniversalPermissionRole.MODERATOR;
        }
        return UniversalPermissionRole.MEMBER;
    }

    private DiscordPermissionMapping permissionMapping() {
        return getDiscordBotConfig().permissionMapping() == null ? DiscordPermissionMapping.empty() : getDiscordBotConfig().permissionMapping();
    }

    private boolean matchesAny(Set<String> actualValues, Set<String> expectedValues) {
        return actualValues.stream().anyMatch(actual -> expectedValues.stream().anyMatch(expected -> expected.equalsIgnoreCase(actual)));
    }
}
