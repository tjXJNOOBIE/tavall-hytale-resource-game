package com.tavall.hytale.resourcegame.frontend.hytale;

import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandEnvelope;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandVerificationResult;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendControlCommandClient;
import com.tjxjnoobie.api.platform.global.console.Log;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class HytaleControlPlaneCommandBridge {
    private final HytaleKdCommandEnvelopeBridge kdCommandEnvelopeBridge;
    private final FrontendControlCommandClient controlCommandClient;

    public HytaleControlPlaneCommandBridge(
            HytaleKdCommandEnvelopeBridge kdCommandEnvelopeBridge,
            FrontendControlCommandClient controlCommandClient
    ) {
        this.kdCommandEnvelopeBridge = Objects.requireNonNull(kdCommandEnvelopeBridge, "kdCommandEnvelopeBridge");
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
        Log.info("Submitting Hytale frontend command to control plane correlationId=" + correlationId);
        return controlCommandClient.submitCommand(envelope);
    }
}
