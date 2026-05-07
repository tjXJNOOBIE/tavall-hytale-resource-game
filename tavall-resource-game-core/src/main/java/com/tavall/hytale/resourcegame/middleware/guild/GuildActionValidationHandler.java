package com.tavall.hytale.resourcegame.middleware.guild;

public final class GuildActionValidationHandler {
    private final GuildPermissionValidationHandler guildPermissionValidationHandler;

    public GuildActionValidationHandler(GuildPermissionValidationHandler guildPermissionValidationHandler) {
        this.guildPermissionValidationHandler = guildPermissionValidationHandler;
    }

    public boolean validateKingdomActionState(
            GuildKingdom guildKingdom,
            GuildMemberProfile actor,
            GuildActionRequirement requirement,
            boolean highRiskChallengePassed
    ) {
        if (guildKingdom.state() == KingdomState.CLOSED && actor.authorityTier() != GuildAuthorityTier.RULER) {
            return false;
        }
        if (!actor.authorityTier().atLeast(requirement.minimumTier())) {
            return false;
        }
        if (!guildPermissionValidationHandler.hasRequiredPermissions(actor, guildKingdom.tierPolicy(), requirement.requiredPermissions())) {
            return false;
        }
        return requirement.highRiskAction().isEmpty() || highRiskChallengePassed;
    }
}
