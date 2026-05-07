package com.tavall.hytale.resourcegame.middleware.cloud;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public record JoinToken(
        UUID joinTokenId,
        String tokenHash,
        UUID createdBy,
        Instant expiresAt,
        Optional<Instant> consumedAt,
        Optional<String> allowedProvider,
        Optional<String> allowedRegion,
        Set<CloudNodeCapability> allowedCapabilities,
        Map<String, String> metadata
) {
    public JoinToken {
        consumedAt = consumedAt == null ? Optional.empty() : consumedAt;
        allowedProvider = allowedProvider == null ? Optional.empty() : allowedProvider;
        allowedRegion = allowedRegion == null ? Optional.empty() : allowedRegion;
        allowedCapabilities = allowedCapabilities == null ? Set.of() : Set.copyOf(allowedCapabilities);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public boolean activeAt(Instant now) {
        return consumedAt.isEmpty() && now.isBefore(expiresAt);
    }

    public JoinToken consumed(Instant now) {
        return new JoinToken(joinTokenId, tokenHash, createdBy, expiresAt, Optional.of(now), allowedProvider, allowedRegion,
                allowedCapabilities, metadata);
    }
}
