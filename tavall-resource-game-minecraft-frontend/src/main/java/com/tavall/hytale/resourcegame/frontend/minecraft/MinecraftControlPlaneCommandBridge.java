package com.tavall.hytale.resourcegame.frontend.minecraft;

import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandEnvelope;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandSurface;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandVerificationResult;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendControlCommandClient;
import com.tjxjnoobie.api.platform.global.console.Log;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class MinecraftControlPlaneCommandBridge {
    private final MinecraftKdCommandEnvelopeBridge kdCommandEnvelopeBridge;
    private final MinecraftFrontendCommandEnvelopeFactory envelopeFactory;
    private final FrontendControlCommandClient controlCommandClient;

    public MinecraftControlPlaneCommandBridge(
            MinecraftKdCommandEnvelopeBridge kdCommandEnvelopeBridge,
            FrontendControlCommandClient controlCommandClient
    ) {
        this.kdCommandEnvelopeBridge = Objects.requireNonNull(kdCommandEnvelopeBridge, "kdCommandEnvelopeBridge");
        this.envelopeFactory = new MinecraftFrontendCommandEnvelopeFactory();
        this.controlCommandClient = Objects.requireNonNull(controlCommandClient, "controlCommandClient");
    }

    public FrontendCommandVerificationResult submitKdCommand(
            String platformAccountId,
            String platformDisplayName,
            List<String> commandTokens,
            String correlationId,
            Map<String, String> sourceMetadata
    ) {
        FrontendCommandEnvelope envelope = kdCommandEnvelopeBridge.commandEnvelope(
                platformAccountId,
                platformDisplayName,
                commandTokens,
                correlationId,
                sourceMetadata
        );
        Log.info("Submitting Minecraft frontend command to control plane correlationId=" + correlationId);
        return controlCommandClient.submitCommand(envelope);
    }

    public FrontendCommandVerificationResult submitAction(
            FrontendCommandSurface surface,
            String platformAccountId,
            String platformDisplayName,
            String actionId,
            Map<String, String> actionArguments,
            String correlationId,
            Map<String, String> sourceMetadata
    ) {
        FrontendCommandEnvelope envelope = envelopeFactory.actionEnvelope(surface, platformAccountId, platformDisplayName, actionId, actionArguments, correlationId, sourceMetadata);
        Log.info("Submitting Minecraft frontend action to control plane correlationId=" + correlationId);
        return controlCommandClient.submitCommand(envelope);
    }
}
