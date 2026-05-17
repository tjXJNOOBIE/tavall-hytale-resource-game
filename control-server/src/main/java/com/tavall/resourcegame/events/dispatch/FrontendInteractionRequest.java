package com.tavall.resourcegame.events.dispatch;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record FrontendInteractionRequest(
        UUID interactionId,
        UUID actorId,
        String actionId,
        String targetId,
        Instant occurredAt,
        Map<String, String> payload
) {
    public FrontendInteractionRequest {
        interactionId = interactionId == null ? UUID.randomUUID() : interactionId;
        if (actorId == null) {
            throw new IllegalArgumentException("actorId is required.");
        }
        if (actionId == null || actionId.isBlank()) {
            throw new IllegalArgumentException("actionId is required.");
        }
        targetId = targetId == null ? "" : targetId;
        occurredAt = occurredAt == null ? Instant.now() : occurredAt;
        payload = payload == null ? Map.of() : Map.copyOf(payload);
    }
}
