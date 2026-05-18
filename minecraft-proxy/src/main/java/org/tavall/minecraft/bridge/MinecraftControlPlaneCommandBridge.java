package org.tavall.minecraft.bridge;

import org.tavall.api.minecraft.frontend.FrontendCommandSurface;
import org.tavall.api.minecraft.frontend.FrontendCommandVerificationResult;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import com.tjxjnoobie.api.platform.global.console.Log;

import java.util.List;
import java.util.Map;

public final class MinecraftControlPlaneCommandBridge implements IMinecraftControlPlaneCommandBridge, IMinecraftFrontendBridgeDependencyAccess, IDependencyInjectableConcrete {
    @Override
    public FrontendCommandVerificationResult submitKdCommand(
            String platformAccountId,
            String platformDisplayName,
            List<String> commandTokens,
            String correlationId,
            Map<String, String> sourceMetadata
    ) {
        Log.info("Submitting Minecraft frontend command to control plane correlationId=" + correlationId);
        return getMinecraftControlCommandClient().submitCommand(getMinecraftKdCommandEnvelopeBridge().commandEnvelope(
                platformAccountId,
                platformDisplayName,
                commandTokens,
                correlationId,
                sourceMetadata
        ));
    }

    @Override
    public FrontendCommandVerificationResult submitAction(
            FrontendCommandSurface surface,
            String platformAccountId,
            String platformDisplayName,
            String actionId,
            Map<String, String> actionArguments,
            String correlationId,
            Map<String, String> sourceMetadata
    ) {
        Log.info("Submitting Minecraft frontend action to control plane correlationId=" + correlationId);
        return getMinecraftControlCommandClient().submitCommand(getMinecraftFrontendCommandEnvelopeFactory().actionEnvelope(surface, platformAccountId, platformDisplayName, actionId, actionArguments, correlationId, sourceMetadata));
    }
}
