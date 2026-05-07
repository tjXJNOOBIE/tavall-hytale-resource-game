package com.tavall.hytale.resourcegame.frontend.hytale;

import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandEnvelope;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandEnvelopeFactory;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandSurface;
import com.tavall.hytale.resourcegame.shared.frontend.ResourceGameFrontendPlatform;
import com.tjxjnoobie.api.platform.global.console.Log;

import java.util.Map;

public final class HytaleFrontendCommandEnvelopeFactory implements FrontendCommandEnvelopeFactory {
    @Override
    public ResourceGameFrontendPlatform platform() {
        return ResourceGameFrontendPlatform.HYTALE;
    }

    @Override
    public FrontendCommandEnvelope commandEnvelope(
            String platformAccountId,
            String platformDisplayName,
            String rawInput,
            String correlationId,
            Map<String, String> sourceMetadata
    ) {
        Log.info("Hytale frontend command envelope created for correlationId=" + correlationId);
        return FrontendCommandEnvelope.command(platform(), platformAccountId, platformDisplayName, rawInput, correlationId, sourceMetadata);
    }

    @Override
    public FrontendCommandEnvelope actionEnvelope(
            FrontendCommandSurface surface,
            String platformAccountId,
            String platformDisplayName,
            String actionId,
            Map<String, String> arguments,
            String correlationId,
            Map<String, String> sourceMetadata
    ) {
        Log.info("Hytale frontend action envelope created for actionId=" + actionId);
        return FrontendCommandEnvelope.action(platform(), surface, platformAccountId, platformDisplayName, actionId, arguments, correlationId, sourceMetadata);
    }
}
