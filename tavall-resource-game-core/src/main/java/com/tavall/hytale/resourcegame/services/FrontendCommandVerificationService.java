package com.tavall.hytale.resourcegame.services;

import com.tavall.hytale.resourcegame.dependency.IDependencyInjectableConcrete;
import com.tavall.hytale.resourcegame.dependency.interfaces.IFrontendCommandVerificationService;
import com.tavall.hytale.resourcegame.frontend.hytale.HytaleControlPlaneCommandBridge;
import com.tavall.hytale.resourcegame.frontend.hytale.HytaleKdCommandEnvelopeBridge;
import com.tavall.hytale.resourcegame.middleware.control.ControlCommandRuntime;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandVerificationResult;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendControlCommandClient;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class FrontendCommandVerificationService implements IFrontendCommandVerificationService, IDependencyInjectableConcrete {
    private final HytaleControlPlaneCommandBridge hytaleControlPlaneCommandBridge;
    private final String serverId;

    public FrontendCommandVerificationService(
            ControlCommandRuntime controlCommandRuntime,
            HytaleKdCommandEnvelopeBridge hytaleKdCommandEnvelopeBridge
    ) {
        this(
                hytaleKdCommandEnvelopeBridge,
                envelope -> Objects.requireNonNull(controlCommandRuntime, "controlCommandRuntime")
                        .frontendCommandIngressHandler()
                        .ingest(envelope, Instant.now()),
                "hytale-single-server"
        );
    }

    public FrontendCommandVerificationService(
            HytaleKdCommandEnvelopeBridge hytaleKdCommandEnvelopeBridge,
            FrontendControlCommandClient controlCommandClient,
            String serverId
    ) {
        this.hytaleControlPlaneCommandBridge = new HytaleControlPlaneCommandBridge(
                Objects.requireNonNull(hytaleKdCommandEnvelopeBridge, "hytaleKdCommandEnvelopeBridge"),
                Objects.requireNonNull(controlCommandClient, "controlCommandClient")
        );
        this.serverId = Objects.requireNonNull(serverId, "serverId");
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
                mergedSourceMetadata(sourceMetadata)
        );
    }

    /**
     * Stamps Hytale envelopes with the single-server surface identity so the control server can
     * distinguish real game commands from proxy/global commands.
     */
    private Map<String, String> mergedSourceMetadata(Map<String, String> sourceMetadata) {
        java.util.LinkedHashMap<String, String> metadata = new java.util.LinkedHashMap<>();
        metadata.put("server", serverId);
        metadata.put("surfaceIdentity", "HYTALE_SINGLE_SERVER");
        metadata.put("serverDataSource", "hytale-single-server");
        metadata.putAll(sourceMetadata == null ? Map.of() : sourceMetadata);
        return Map.copyOf(metadata);
    }
}
