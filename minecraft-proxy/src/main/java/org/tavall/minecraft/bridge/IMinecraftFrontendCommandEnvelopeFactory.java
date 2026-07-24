package org.tavall.minecraft.bridge;

import org.tavall.api.minecraft.frontend.FrontendCommandEnvelope;
import org.tavall.api.minecraft.frontend.FrontendCommandSurface;
import org.tavall.dependency.IDependencyInjectableInterface;

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
