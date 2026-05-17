package com.tavall.resourcegame.frontend.minecraft.bridge;

import com.tavall.resourcegame.api.internal.frontend.FrontendCommandEnvelope;
import com.tavall.resourcegame.frontend.minecraft.runtime.IMinecraftFrontendDomain;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.util.List;
import java.util.Map;

public final class MinecraftKdCommandEnvelopeBridge implements IMinecraftKdCommandEnvelopeBridge, IMinecraftFrontendDomain, IDependencyInjectableConcrete {
    @Override
    public FrontendCommandEnvelope commandEnvelope(
            String platformAccountId,
            String platformDisplayName,
            List<String> commandTokens,
            String correlationId,
            Map<String, String> sourceMetadata
    ) {
        return getMinecraftFrontendCommandEnvelopeFactory().commandEnvelope(
                platformAccountId,
                platformDisplayName,
                getMinecraftKdCommandInputFormatterHandler().rawKdInput(commandTokens),
                correlationId,
                sourceMetadata
        );
    }
}
