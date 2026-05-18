package org.tavall.api.minecraft.interaction;

import java.util.Map;
import java.util.Objects;

public record InteractionRequest(
        String requestId,
        String playerId,
        InteractionTargetType targetType,
        String targetId,
        String interactionType,
        String serverId,
        String worldName,
        Map<String, String> context,
        long createdAtEpochMillis
) {
    public InteractionRequest {
        Objects.requireNonNull(requestId, "requestId");
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(targetType, "targetType");
        Objects.requireNonNull(targetId, "targetId");
        Objects.requireNonNull(interactionType, "interactionType");
        Objects.requireNonNull(serverId, "serverId");

        if (requestId.isBlank()) {
            throw new IllegalArgumentException("requestId must not be blank");
        }
        if (playerId.isBlank()) {
            throw new IllegalArgumentException("playerId must not be blank");
        }
        if (targetId.isBlank()) {
            throw new IllegalArgumentException("targetId must not be blank");
        }
        if (interactionType.isBlank()) {
            throw new IllegalArgumentException("interactionType must not be blank");
        }
        if (serverId.isBlank()) {
            throw new IllegalArgumentException("serverId must not be blank");
        }

        context = context == null ? Map.of() : Map.copyOf(context);
    }
}
