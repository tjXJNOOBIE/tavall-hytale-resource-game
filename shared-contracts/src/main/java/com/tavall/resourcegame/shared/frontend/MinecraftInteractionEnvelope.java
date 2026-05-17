package com.tavall.resourcegame.shared.frontend;

import java.util.Map;
import java.util.Objects;

public record MinecraftInteractionEnvelope(
        ResourceGameFrontendSurfaceIdentity surfaceIdentity,
        String serverId,
        String platformAccountId,
        String platformDisplayName,
        FrontendCommandSurface interactionSurface,
        String actionId,
        Map<String, String> arguments,
        String correlationId,
        Map<String, String> sourceMetadata
) {
    public MinecraftInteractionEnvelope {
        Objects.requireNonNull(surfaceIdentity, "surfaceIdentity");
        Objects.requireNonNull(serverId, "serverId");
        Objects.requireNonNull(platformAccountId, "platformAccountId");
        Objects.requireNonNull(interactionSurface, "interactionSurface");
        Objects.requireNonNull(actionId, "actionId");
        Objects.requireNonNull(correlationId, "correlationId");

        if (serverId.isBlank()) {
            throw new IllegalArgumentException("serverId must not be blank");
        }
        if (platformAccountId.isBlank()) {
            throw new IllegalArgumentException("platformAccountId must not be blank");
        }
        if (actionId.isBlank()) {
            throw new IllegalArgumentException("actionId must not be blank");
        }
        if (correlationId.isBlank()) {
            throw new IllegalArgumentException("correlationId must not be blank");
        }

        arguments = arguments == null ? Map.of() : Map.copyOf(arguments);
        sourceMetadata = sourceMetadata == null ? Map.of() : Map.copyOf(sourceMetadata);
    }

    public FrontendCommandEnvelope toFrontendCommandEnvelope() {
        return FrontendCommandEnvelope.action(
                ResourceGameFrontendPlatform.MINECRAFT,
                interactionSurface,
                platformAccountId,
                platformDisplayName,
                actionId,
                arguments,
                correlationId,
                sourceMetadata
        );
    }
}
