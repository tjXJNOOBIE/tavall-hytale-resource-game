package com.tavall.resourcegame.frontend.minecraft.bridge;

import com.tavall.resourcegame.api.internal.frontend.FrontendCommandEnvelope;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.util.List;
import java.util.Map;

public interface IMinecraftKdCommandEnvelopeBridge extends IDependencyInjectableInterface {
    FrontendCommandEnvelope commandEnvelope(
            String platformAccountId,
            String platformDisplayName,
            List<String> commandTokens,
            String correlationId,
            Map<String, String> sourceMetadata
    );
}
