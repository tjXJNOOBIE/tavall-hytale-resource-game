package com.tavall.hytale.resourcegame.middleware.identity;

import com.tavall.hytale.resourcegame.middleware.common.GamePlatform;
import com.tavall.hytale.resourcegame.middleware.common.MetadataMaps;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record AccountSession(
        UUID sessionId,
        UniversalPlayerId universalPlayerId,
        Instant createdAt,
        Instant expiresAt,
        Optional<Instant> revokedAt,
        Optional<GamePlatform> platform,
        Map<String, String> metadata
) {
    public AccountSession {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(universalPlayerId, "universalPlayerId");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(expiresAt, "expiresAt");
        revokedAt = revokedAt == null ? Optional.empty() : revokedAt;
        platform = platform == null ? Optional.empty() : platform;
        metadata = MetadataMaps.immutable(metadata);
    }

    public boolean activeAt(Instant now) {
        return revokedAt.isEmpty() && expiresAt.isAfter(now);
    }
}
