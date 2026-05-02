package com.tavall.hytale.resourcegame.shared.frontend;

import java.util.Objects;

public record ResourceGameFrontendActionDescriptor(
        String actionId,
        String label,
        String interactionType
) {
    public ResourceGameFrontendActionDescriptor {
        Objects.requireNonNull(actionId, "actionId");
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(interactionType, "interactionType");

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
}
