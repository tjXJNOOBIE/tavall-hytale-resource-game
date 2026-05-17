package com.tavall.resourcegame.middleware.projection;

import com.tavall.resourcegame.middleware.guild.GuildActionRequirement;
import com.tavall.resourcegame.middleware.guild.GuildAuthorityTier;
import com.tavall.resourcegame.middleware.guild.GuildKingdom;
import com.tavall.resourcegame.middleware.guild.GuildMemberProfile;
import com.tavall.resourcegame.middleware.guild.GuildPermission;
import com.tavall.resourcegame.middleware.guild.GuildActionValidationHandler;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class InteractionActionAvailabilityHandler implements IProjectionDomain {
    public InteractionActionAvailabilityHandler() {
    }

    public InteractionActionAvailabilityHandler(GuildActionValidationHandler guildActionValidationHandler) {
        registerGuildActionValidationHandler(guildActionValidationHandler);
    }

    public InteractionAction actionForRequirement(
            String actionId,
            String label,
            GuildKingdom guildKingdom,
            GuildMemberProfile actor,
            GuildActionRequirement requirement,
            PlatformInteractionType interactionType
    ) {
        boolean enabled = getGuildActionValidationHandler().validateKingdomActionState(guildKingdom, actor, requirement, false);
        Optional<String> disabledReason = enabled ? Optional.empty() : Optional.of("Insufficient guild authority or permission.");
        return new InteractionAction(actionId, label, Optional.of(requirement.minimumTier()), requirement.requiredPermissions(), enabled, disabledReason, interactionType, Map.of());
    }

    public InteractionAction simpleEnabledAction(String actionId, String label, PlatformInteractionType interactionType) {
        return new InteractionAction(actionId, label, Optional.empty(), Set.of(), true, Optional.empty(), interactionType, Map.of());
    }

    public InteractionAction disabledAction(String actionId, String label, GuildAuthorityTier requiredTier, Set<GuildPermission> permissions, PlatformInteractionType interactionType, String reason) {
        return new InteractionAction(actionId, label, Optional.of(requiredTier), permissions, false, Optional.of(reason), interactionType, Map.of());
    }
}
