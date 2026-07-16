package org.tavall.control.projection;

import org.tavall.control.common.MetadataMaps;
import org.tavall.control.guild.GuildAuthorityTier;
import org.tavall.control.guild.GuildPermission;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public record InteractionAction(
        String actionId,
        String label,
        Optional<GuildAuthorityTier> requiredTier,
        Set<GuildPermission> requiredPermissions,
        boolean enabled,
        Optional<String> disabledReason,
        PlatformInteractionType interactionType,
        Map<String, String> metadata
) {
    public InteractionAction {
        if (actionId == null || actionId.isBlank()) {
            throw new IllegalArgumentException("actionId is required.");
        }
        label = label == null || label.isBlank() ? actionId : label;
        requiredTier = requiredTier == null ? Optional.empty() : requiredTier;
        requiredPermissions = requiredPermissions == null ? Set.of() : Set.copyOf(requiredPermissions);
        disabledReason = disabledReason == null ? Optional.empty() : disabledReason;
        Objects.requireNonNull(interactionType, "interactionType");
        metadata = MetadataMaps.immutable(metadata);
    }
}
