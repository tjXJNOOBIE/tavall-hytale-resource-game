package com.tavall.hytale.resourcegame.middleware.guild;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public record GuildTierPolicy(
        Map<GuildAuthorityTier, Set<GuildPermission>> tierPermissions,
        boolean explicitPermissionExpansionAllowed
) {
    public GuildTierPolicy {
        EnumMap<GuildAuthorityTier, Set<GuildPermission>> copy = new EnumMap<>(GuildAuthorityTier.class);
        if (tierPermissions != null) {
            for (Map.Entry<GuildAuthorityTier, Set<GuildPermission>> entry : tierPermissions.entrySet()) {
                copy.put(entry.getKey(), entry.getValue() == null ? Set.of() : Set.copyOf(entry.getValue()));
            }
        }
        tierPermissions = Map.copyOf(copy);
    }

    public static GuildTierPolicy defaults() {
        EnumMap<GuildAuthorityTier, Set<GuildPermission>> permissions = new EnumMap<>(GuildAuthorityTier.class);
        permissions.put(GuildAuthorityTier.OUTSIDER, Set.of());
        permissions.put(GuildAuthorityTier.CITIZEN, Set.of(GuildPermission.CREATE_PETITION));
        permissions.put(GuildAuthorityTier.TRUSTED, Set.of(GuildPermission.CREATE_PETITION));
        permissions.put(GuildAuthorityTier.OFFICER, Set.of(GuildPermission.CREATE_PETITION, GuildPermission.INVITE_PLAYER));
        permissions.put(GuildAuthorityTier.COUNCIL, Set.of(
                GuildPermission.CREATE_PETITION,
                GuildPermission.INVITE_PLAYER,
                GuildPermission.MANAGE_TAX_POLICY,
                GuildPermission.VIEW_TREASURY,
                GuildPermission.MANAGE_BUILDINGS,
                GuildPermission.SPEND_TREASURY
        ));
        permissions.put(GuildAuthorityTier.RULER, EnumSet.allOf(GuildPermission.class));
        return new GuildTierPolicy(permissions, true);
    }

    public Set<GuildPermission> permissionsForTier(GuildAuthorityTier authorityTier) {
        return tierPermissions.getOrDefault(authorityTier, Set.of());
    }
}
