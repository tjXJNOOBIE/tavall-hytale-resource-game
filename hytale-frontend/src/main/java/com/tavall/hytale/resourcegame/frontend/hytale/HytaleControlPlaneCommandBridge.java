package com.tavall.hytale.resourcegame.frontend.hytale;

import com.tavall.resourcegame.shared.frontend.FrontendCommandSurface;
import com.tavall.resourcegame.shared.frontend.FrontendCommandVerificationResult;
import com.tjxjnoobie.api.platform.global.console.Log;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.util.List;
import java.util.Map;

public final class HytaleControlPlaneCommandBridge implements IHytaleControlPlaneCommandBridge, IHytaleFrontendDomain, IDependencyInjectableConcrete {
    @Override
    public FrontendCommandVerificationResult submitKdCommand(
            String platformAccountId,
            String platformDisplayName,
            List<String> commandTokens,
            String correlationId,
            Map<String, String> sourceMetadata
    ) {
        Log.info("Submitting Hytale frontend command to control plane correlationId=" + correlationId);
        return getHytaleControlCommandClient().submitCommand(getHytaleKdCommandEnvelopeBridge().commandEnvelope(
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
        Log.info("Submitting Hytale frontend action to control plane correlationId=" + correlationId);
        return getHytaleControlCommandClient().submitCommand(getHytaleFrontendCommandEnvelopeFactory().actionEnvelope(surface, platformAccountId, platformDisplayName, actionId, actionArguments, correlationId, sourceMetadata));
    }
}
