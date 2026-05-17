package com.tavall.resourcegame.api.internal.frontend;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;

public record FrontendCommandEnvelope(
        @JsonProperty("platform")
        ResourceGameFrontendPlatform platform,
        @JsonProperty("surface")
        FrontendCommandSurface surface,
        @JsonProperty("platformAccountId")
        String platformAccountId,
        @JsonProperty("platformDisplayName")
        String platformDisplayName,
        @JsonProperty("rawInput")
        String rawInput,
        @JsonProperty("actionId")
        String actionId,
        @JsonProperty("arguments")
        Map<String, String> arguments,
        @JsonProperty("correlationId")
        String correlationId,
        @JsonProperty("sourceMetadata")
        Map<String, String> sourceMetadata
) {
    @JsonCreator
    public FrontendCommandEnvelope {
        Objects.requireNonNull(platform, "platform");
        Objects.requireNonNull(surface, "surface");
        Objects.requireNonNull(correlationId, "correlationId");

        if (correlationId.isBlank()) {
            throw new IllegalArgumentException("correlationId must not be blank");
        }

        boolean hasRawInput = rawInput != null && !rawInput.isBlank();
        boolean hasActionId = actionId != null && !actionId.isBlank();
        if (!hasRawInput && !hasActionId) {
            throw new IllegalArgumentException("rawInput or actionId is required");
        }

        arguments = arguments == null ? Map.of() : Map.copyOf(arguments);
        sourceMetadata = sourceMetadata == null ? Map.of() : Map.copyOf(sourceMetadata);
    }

    public static FrontendCommandEnvelope command(
            ResourceGameFrontendPlatform platform,
            String platformAccountId,
            String platformDisplayName,
            String rawInput,
            String correlationId,
            Map<String, String> sourceMetadata
    ) {
        return new FrontendCommandEnvelope(
                platform,
                FrontendCommandSurface.COMMAND,
                platformAccountId,
                platformDisplayName,
                rawInput,
                null,
                Map.of(),
                correlationId,
                sourceMetadata
        );
    }

    public static FrontendCommandEnvelope action(
            ResourceGameFrontendPlatform platform,
            FrontendCommandSurface surface,
            String platformAccountId,
            String platformDisplayName,
            String actionId,
            Map<String, String> arguments,
            String correlationId,
            Map<String, String> sourceMetadata
    ) {
        return new FrontendCommandEnvelope(
                platform,
                surface,
                platformAccountId,
                platformDisplayName,
                null,
                actionId,
                arguments,
                correlationId,
                sourceMetadata
        );
    }
}
