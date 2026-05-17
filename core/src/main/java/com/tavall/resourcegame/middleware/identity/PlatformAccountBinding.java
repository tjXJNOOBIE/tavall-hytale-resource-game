package com.tavall.resourcegame.middleware.identity;

import com.tavall.resourcegame.middleware.common.GamePlatform;
import com.tavall.resourcegame.middleware.common.MetadataMaps;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record PlatformAccountBinding(
        UUID bindingId,
        UniversalPlayerId universalPlayerId,
        GamePlatform platform,
        String platformAccountId,
        String platformDisplayName,
        boolean verified,
        Instant linkedAt,
        Instant lastSeenAt,
        Map<String, String> metadata
) {
    public PlatformAccountBinding {
        Objects.requireNonNull(bindingId, "bindingId");
        Objects.requireNonNull(universalPlayerId, "universalPlayerId");
        Objects.requireNonNull(platform, "platform");
        if (platformAccountId == null || platformAccountId.isBlank()) {
            throw new IllegalArgumentException("platformAccountId is required.");
        }
        platformDisplayName = platformDisplayName == null ? "" : platformDisplayName;
        Objects.requireNonNull(linkedAt, "linkedAt");
        Objects.requireNonNull(lastSeenAt, "lastSeenAt");
        metadata = MetadataMaps.immutable(metadata);
    }
}
