package com.tavall.hytale.resourcegame.middleware.guild;

import com.tavall.hytale.resourcegame.middleware.common.HighRiskAction;

import java.util.Optional;
import java.util.Set;

public record GuildActionRequirement(
        GuildAuthorityTier minimumTier,
        Set<GuildPermission> requiredPermissions,
        Optional<HighRiskAction> highRiskAction
) {
    public GuildActionRequirement {
        minimumTier = minimumTier == null ? GuildAuthorityTier.CITIZEN : minimumTier;
        requiredPermissions = requiredPermissions == null ? Set.of() : Set.copyOf(requiredPermissions);
        highRiskAction = highRiskAction == null ? Optional.empty() : highRiskAction;
    }

    public static GuildActionRequirement of(GuildAuthorityTier minimumTier, GuildPermission requiredPermission) {
        return new GuildActionRequirement(minimumTier, Set.of(requiredPermission), Optional.empty());
    }
}
