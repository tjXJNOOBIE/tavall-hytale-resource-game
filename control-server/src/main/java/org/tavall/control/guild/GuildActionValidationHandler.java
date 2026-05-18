package org.tavall.control.guild;

public final class GuildActionValidationHandler implements GuildDomain {
    public GuildActionValidationHandler() {
    }

    public GuildActionValidationHandler(GuildPermissionValidationHandler guildPermissionValidationHandler) {
        registerGuildPermissionValidationHandler(guildPermissionValidationHandler);
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
        if (!getGuildPermissionValidationHandler().hasRequiredPermissions(actor, guildKingdom.tierPolicy(), requirement.requiredPermissions())) {
            return false;
        }
        return requirement.highRiskAction().isEmpty() || highRiskChallengePassed;
    }
}
