package com.tavall.resourcegame.api.internal.minecraft;

import com.tavall.resourcegame.api.internal.frontend.ResourceGameFrontendSurfaceIdentity;

import java.util.Map;
import java.util.Objects;

public record MinecraftVisualRenderRequest(
        ResourceGameFrontendSurfaceIdentity targetSurfaceIdentity,
        String serverId,
        String targetPlayerId,
        String visualType,
        String title,
        String body,
        Map<String, String> payload,
        String correlationId
) {
    public MinecraftVisualRenderRequest {
        Objects.requireNonNull(targetSurfaceIdentity, "targetSurfaceIdentity");
        Objects.requireNonNull(serverId, "serverId");
        Objects.requireNonNull(visualType, "visualType");
        Objects.requireNonNull(correlationId, "correlationId");

        if (serverId.isBlank()) {
            throw new IllegalArgumentException("serverId must not be blank");
        }
        if (visualType.isBlank()) {
            throw new IllegalArgumentException("visualType must not be blank");
        }
        if (correlationId.isBlank()) {
            throw new IllegalArgumentException("correlationId must not be blank");
        }

        payload = payload == null ? Map.of() : Map.copyOf(payload);
    }
}
