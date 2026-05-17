package com.tavall.resourcegame.frontend.roblox;

import com.tavall.resourcegame.shared.frontend.FrontendCommandEnvelope;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.util.List;
import java.util.Map;

public final class RobloxKdCommandEnvelopeBridge implements IRobloxKdCommandEnvelopeBridge, IRobloxFrontendDomain, IDependencyInjectableConcrete {
    @Override
    public FrontendCommandEnvelope commandEnvelope(
            String platformAccountId,
            String platformDisplayName,
            List<String> commandTokens,
            String correlationId,
            Map<String, String> sourceMetadata
    ) {
        return getRobloxFrontendCommandEnvelopeFactory().commandEnvelope(
                platformAccountId,
                platformDisplayName,
                getRobloxKdCommandInputFormatterHandler().rawKdInput(commandTokens),
                correlationId,
                sourceMetadata
        );
    }
}
