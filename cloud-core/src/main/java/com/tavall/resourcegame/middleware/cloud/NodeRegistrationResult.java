package com.tavall.resourcegame.middleware.cloud;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public record NodeRegistrationResult(
        boolean success,
        Optional<UUID> nodeId,
        Optional<UUID> agentId,
        Optional<String> issuedIdentityMaterial,
        String message,
        Map<String, String> metadata
) {
    public NodeRegistrationResult {
        nodeId = nodeId == null ? Optional.empty() : nodeId;
        agentId = agentId == null ? Optional.empty() : agentId;
        issuedIdentityMaterial = issuedIdentityMaterial == null ? Optional.empty() : issuedIdentityMaterial;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static NodeRegistrationResult failed(String message) {
        return new NodeRegistrationResult(false, Optional.empty(), Optional.empty(), Optional.empty(), message, Map.of());
    }
}
