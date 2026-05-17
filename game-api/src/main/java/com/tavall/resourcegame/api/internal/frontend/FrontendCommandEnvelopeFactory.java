package com.tavall.resourcegame.api.internal.frontend;

import java.util.Map;

public interface FrontendCommandEnvelopeFactory {
    ResourceGameFrontendPlatform platform();

    FrontendCommandEnvelope commandEnvelope(
            String platformAccountId,
            String platformDisplayName,
            String rawInput,
            String correlationId,
            Map<String, String> sourceMetadata
    );

    FrontendCommandEnvelope actionEnvelope(
            FrontendCommandSurface surface,
            String platformAccountId,
            String platformDisplayName,
            String actionId,
            Map<String, String> arguments,
            String correlationId,
            Map<String, String> sourceMetadata
    );
}
