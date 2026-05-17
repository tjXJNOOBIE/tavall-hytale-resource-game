package com.tavall.resourcegame.frontend.minecraft.bridge;

import com.tavall.resourcegame.api.internal.frontend.FrontendCommandEnvelope;
import com.tavall.resourcegame.api.internal.frontend.FrontendCommandSurface;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.util.Map;

public interface IMinecraftFrontendCommandEnvelopeFactory extends IDependencyInjectableInterface {
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
            Map<String, String> actionArguments,
            String correlationId,
            Map<String, String> sourceMetadata
    );
}
