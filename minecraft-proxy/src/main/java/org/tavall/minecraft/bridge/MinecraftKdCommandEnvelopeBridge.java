package org.tavall.minecraft.bridge;

import org.tavall.api.minecraft.frontend.FrontendCommandEnvelope;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.util.List;
import java.util.Map;

public final class MinecraftKdCommandEnvelopeBridge implements IMinecraftKdCommandEnvelopeBridge, IMinecraftFrontendBridgeDependencyAccess, IDependencyInjectableConcrete {
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
