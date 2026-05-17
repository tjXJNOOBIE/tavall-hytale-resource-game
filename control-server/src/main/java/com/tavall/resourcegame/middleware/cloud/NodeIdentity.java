package com.tavall.resourcegame.middleware.cloud;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public record NodeIdentity(
        UUID nodeId,
        UUID agentId,
        Optional<String> publicKey,
        Optional<String> sharedSecretHash,
        String identityStatus,
        Instant createdAt,
        Optional<Instant> rotatedAt,
        Map<String, String> metadata
) {
    public NodeIdentity {
        publicKey = publicKey == null ? Optional.empty() : publicKey;
        sharedSecretHash = sharedSecretHash == null ? Optional.empty() : sharedSecretHash;
        rotatedAt = rotatedAt == null ? Optional.empty() : rotatedAt;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
