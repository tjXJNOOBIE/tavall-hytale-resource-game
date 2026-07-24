package org.tavall.minecraft.bridge;

import org.tavall.api.minecraft.frontend.FrontendCommandEnvelope;
import org.tavall.dependency.IDependencyInjectableInterface;

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
