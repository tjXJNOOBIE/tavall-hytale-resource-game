package com.tavall.hytale.resourcegame.services;

import com.tavall.hytale.resourcegame.dependency.IDependencyInjectableConcrete;
import com.tavall.hytale.resourcegame.dependency.interfaces.IFrontendCommandVerificationService;
import com.tavall.hytale.resourcegame.frontend.hytale.HytaleControlPlaneCommandBridge;
import com.tavall.hytale.resourcegame.frontend.hytale.HytaleKdCommandEnvelopeBridge;
import com.tavall.hytale.resourcegame.middleware.control.ControlCommandRuntime;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandVerificationResult;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class FrontendCommandVerificationService implements IFrontendCommandVerificationService, IDependencyInjectableConcrete {
    private final HytaleControlPlaneCommandBridge hytaleControlPlaneCommandBridge;

    public FrontendCommandVerificationService(
            ControlCommandRuntime controlCommandRuntime,
            HytaleKdCommandEnvelopeBridge hytaleKdCommandEnvelopeBridge
    ) {
        ControlCommandRuntime runtime = Objects.requireNonNull(controlCommandRuntime, "controlCommandRuntime");
        this.hytaleControlPlaneCommandBridge = new HytaleControlPlaneCommandBridge(
                Objects.requireNonNull(hytaleKdCommandEnvelopeBridge, "hytaleKdCommandEnvelopeBridge"),
                envelope -> runtime.frontendCommandIngressHandler().ingest(envelope, Instant.now())
        );
    }

    @Override
    public FrontendCommandVerificationResult verifyHytaleKdCommand(
            String platformAccountId,
            String platformDisplayName,
            List<String> commandTokens,
            Map<String, String> sourceMetadata
    ) {
        return hytaleControlPlaneCommandBridge.submitKdCommand(
                platformAccountId,
                platformDisplayName,
                commandTokens,
                "hytale-kd-" + UUID.randomUUID(),
                sourceMetadata
        );
    }
}
