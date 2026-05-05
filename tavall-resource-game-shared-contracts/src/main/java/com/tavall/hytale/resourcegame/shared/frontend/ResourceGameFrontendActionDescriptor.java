package com.tavall.hytale.resourcegame.shared.frontend;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public record ResourceGameFrontendActionDescriptor(
        String actionId,
        String label,
        String interactionType,
        Optional<String> requiredTier,
        Set<String> requiredPermissions
) {
    public ResourceGameFrontendActionDescriptor {
        Objects.requireNonNull(actionId, "actionId");
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(interactionType, "interactionType");
        requiredTier = requiredTier == null ? Optional.empty() : requiredTier;
        requiredPermissions = requiredPermissions == null ? Set.of() : Set.copyOf(requiredPermissions);

        if (actionId.isBlank()) {
            throw new IllegalArgumentException("actionId must not be blank");
        }

        if (label.isBlank()) {
            throw new IllegalArgumentException("label must not be blank");
        }

        if (interactionType.isBlank()) {
            throw new IllegalArgumentException("interactionType must not be blank");
        }
    }

    public ResourceGameFrontendActionDescriptor(String actionId, String label, String interactionType) {
        this(actionId, label, interactionType, Optional.empty(), Set.of());
    }
}
