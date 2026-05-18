package org.tavall.control.projection;

import org.tavall.control.guild.GuildActionRequirement;
import org.tavall.control.guild.GuildAuthorityTier;
import org.tavall.control.guild.GuildKingdom;
import org.tavall.control.guild.GuildMemberProfile;
import org.tavall.control.guild.GuildPermission;
import org.tavall.control.guild.GuildActionValidationHandler;

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
