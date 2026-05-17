package com.tavall.resourcegame.middleware.asset;

import com.tavall.resourcegame.middleware.common.GamePlatform;
import com.tavall.resourcegame.middleware.common.MetadataMaps;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record PlatformAssetVersion(
        UUID platformAssetVersionId,
        GlobalAssetId globalAssetId,
        GamePlatform platform,
        String assetReference,
        int version,
        Optional<String> contentHash,
        boolean active,
        Instant createdAt,
        Map<String, String> metadata
) {
    public PlatformAssetVersion {
        Objects.requireNonNull(platformAssetVersionId, "platformAssetVersionId");
        Objects.requireNonNull(globalAssetId, "globalAssetId");
        Objects.requireNonNull(platform, "platform");
        if (assetReference == null || assetReference.isBlank()) {
            throw new IllegalArgumentException("assetReference is required.");
        }
        version = Math.max(1, version);
        contentHash = contentHash == null ? Optional.empty() : contentHash;
        Objects.requireNonNull(createdAt, "createdAt");
        metadata = MetadataMaps.immutable(metadata);
    }
}
