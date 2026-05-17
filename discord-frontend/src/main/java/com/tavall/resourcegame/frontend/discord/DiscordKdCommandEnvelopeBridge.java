package com.tavall.resourcegame.frontend.discord;

import com.tavall.resourcegame.shared.frontend.FrontendCommandEnvelope;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.util.List;
import java.util.Map;

public final class DiscordKdCommandEnvelopeBridge implements IDiscordKdCommandEnvelopeBridge, IDiscordFrontendDomain, IDependencyInjectableConcrete {
    @Override
    public FrontendCommandEnvelope commandEnvelope(
            String platformAccountId,
            String platformDisplayName,
            List<String> commandTokens,
            String correlationId,
            Map<String, String> sourceMetadata
    ) {
        return getDiscordFrontendCommandEnvelopeFactory().commandEnvelope(
                platformAccountId,
                platformDisplayName,
                getDiscordKdCommandInputFormatterHandler().rawKdInput(commandTokens),
                correlationId,
                sourceMetadata
        );
    }
}
