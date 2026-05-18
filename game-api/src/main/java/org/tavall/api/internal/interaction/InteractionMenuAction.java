package org.tavall.api.minecraft.interaction;

import java.util.Map;
import java.util.Objects;

public record InteractionMenuAction(
        String actionId,
        String actionType,
        boolean enabled,
        String disabledReason,
        Map<String, String> metadata
) {
    public InteractionMenuAction {
        Objects.requireNonNull(actionId, "actionId");
        Objects.requireNonNull(actionType, "actionType");
        if (actionId.isBlank()) {
            throw new IllegalArgumentException("actionId must not be blank");
        }
        if (actionType.isBlank()) {
            throw new IllegalArgumentException("actionType must not be blank");
        }
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
