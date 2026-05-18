package org.tavall.control.guild;

import java.util.EnumSet;
import java.util.Set;

public final class GuildPermissionValidationHandler {
    public boolean hasRequiredPermissions(GuildMemberProfile memberProfile, GuildTierPolicy tierPolicy, Set<GuildPermission> requiredPermissions) {
        if (memberProfile == null || memberProfile.authorityTier() == GuildAuthorityTier.OUTSIDER) {
            return false;
        }
        EnumSet<GuildPermission> effectivePermissions = EnumSet.noneOf(GuildPermission.class);
        effectivePermissions.addAll(tierPolicy.permissionsForTier(memberProfile.authorityTier()));
        if (tierPolicy.explicitPermissionExpansionAllowed()) {
            effectivePermissions.addAll(memberProfile.explicitPermissions());
        }
        return effectivePermissions.containsAll(requiredPermissions);
    }
}
