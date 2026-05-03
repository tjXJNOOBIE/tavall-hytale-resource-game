package com.tavall.hytale.resourcegame.services;

import com.tavall.hytale.resourcegame.dependency.IDependencyInjectableConcrete;
import com.tavall.hytale.resourcegame.dependency.interfaces.IFrontendCommandVerificationService;
import com.tavall.hytale.resourcegame.frontend.hytale.HytaleKdCommandEnvelopeBridge;
import com.tavall.hytale.resourcegame.middleware.control.ControlCommandRuntime;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandEnvelope;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandVerificationResult;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class FrontendCommandVerificationService implements IFrontendCommandVerificationService, IDependencyInjectableConcrete {
    private final ControlCommandRuntime controlCommandRuntime;
    private final HytaleKdCommandEnvelopeBridge hytaleKdCommandEnvelopeBridge;

    public FrontendCommandVerificationService(
            ControlCommandRuntime controlCommandRuntime,
            HytaleKdCommandEnvelopeBridge hytaleKdCommandEnvelopeBridge
    ) {
        this.controlCommandRuntime = Objects.requireNonNull(controlCommandRuntime, "controlCommandRuntime");
        this.hytaleKdCommandEnvelopeBridge = Objects.requireNonNull(hytaleKdCommandEnvelopeBridge, "hytaleKdCommandEnvelopeBridge");
    }

    @Override
    public FrontendCommandVerificationResult verifyHytaleKdCommand(
            String platformAccountId,
            String platformDisplayName,
            List<String> commandTokens,
            Map<String, String> sourceMetadata
    ) {
        FrontendCommandEnvelope envelope = hytaleKdCommandEnvelopeBridge.commandEnvelope(
                platformAccountId,
                platformDisplayName,
                commandTokens,
                "hytale-kd-" + UUID.randomUUID(),
                sourceMetadata
        );
        return controlCommandRuntime.frontendCommandIngressHandler().ingest(envelope, Instant.now());
    }
}
