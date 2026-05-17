package com.tavall.resourcegame.shared.frontend;

import java.util.Map;
import java.util.Objects;

public record InteractionResult(
        String requestId,
        InteractionResultType resultType,
        boolean success,
        String message,
        InteractionMenuModel menu,
        String disabledReason,
        Map<String, String> metadata
) {
    public InteractionResult {
        Objects.requireNonNull(requestId, "requestId");
        Objects.requireNonNull(resultType, "resultType");
        Objects.requireNonNull(message, "message");
        if (requestId.isBlank()) {
            throw new IllegalArgumentException("requestId must not be blank");
        }
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
