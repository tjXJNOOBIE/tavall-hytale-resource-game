package com.tavall.resourcegame.middleware.identity;

import com.tavall.resourcegame.middleware.common.GamePlatform;
import com.tavall.resourcegame.middleware.common.MetadataMaps;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record PlatformLinkChallenge(
        UUID challengeId,
        Optional<UniversalPlayerId> universalPlayerId,
        GamePlatform platform,
        Optional<String> platformAccountId,
        String shortCodeHash,
        Instant expiresAt,
        Optional<Instant> consumedAt,
        Instant createdAt,
        Map<String, String> metadata
) {
    public PlatformLinkChallenge {
        Objects.requireNonNull(challengeId, "challengeId");
        universalPlayerId = universalPlayerId == null ? Optional.empty() : universalPlayerId;
        Objects.requireNonNull(platform, "platform");
        platformAccountId = platformAccountId == null ? Optional.empty() : platformAccountId;
        if (shortCodeHash == null || shortCodeHash.isBlank()) {
            throw new IllegalArgumentException("shortCodeHash is required.");
        }
        Objects.requireNonNull(expiresAt, "expiresAt");
        consumedAt = consumedAt == null ? Optional.empty() : consumedAt;
        Objects.requireNonNull(createdAt, "createdAt");
        metadata = MetadataMaps.immutable(metadata);
    }

    public PlatformLinkChallenge claimed(UniversalPlayerId universalPlayerId, String platformAccountId, Instant now) {
        return new PlatformLinkChallenge(
                challengeId,
                Optional.of(universalPlayerId),
                platform,
                Optional.ofNullable(platformAccountId),
                shortCodeHash,
                expiresAt,
                Optional.of(now),
                createdAt,
                metadata
        );
    }
}
