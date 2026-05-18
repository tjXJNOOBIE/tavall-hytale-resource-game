package org.tavall.minecraft.bridge;

import org.tavall.api.minecraft.frontend.FrontendCommandEnvelope;
import org.tavall.api.minecraft.frontend.FrontendCommandEnvelopeFactory;
import org.tavall.api.minecraft.frontend.FrontendCommandSurface;
import org.tavall.api.minecraft.frontend.ResourceGameFrontendPlatform;
import com.tjxjnoobie.api.platform.global.console.Log;

import java.util.Map;

public final class MinecraftFrontendCommandEnvelopeFactory implements FrontendCommandEnvelopeFactory, IMinecraftFrontendCommandEnvelopeFactory, com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete {
    @Override
    public ResourceGameFrontendPlatform platform() {
        return ResourceGameFrontendPlatform.MINECRAFT;
    }

    @Override
    public FrontendCommandEnvelope commandEnvelope(
            String platformAccountId,
            String platformDisplayName,
            String rawInput,
            String correlationId,
            Map<String, String> sourceMetadata
    ) {
        Log.info("Minecraft frontend command envelope created for correlationId=" + correlationId);
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
        Log.info("Minecraft frontend action envelope created for actionId=" + actionId);
        return FrontendCommandEnvelope.action(platform(), surface, platformAccountId, platformDisplayName, actionId, arguments, correlationId, sourceMetadata);
    }
}
