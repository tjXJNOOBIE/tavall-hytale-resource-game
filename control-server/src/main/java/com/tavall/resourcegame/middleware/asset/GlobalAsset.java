package com.tavall.resourcegame.middleware.asset;

import com.tavall.resourcegame.middleware.common.MetadataMaps;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record GlobalAsset(
        GlobalAssetId globalAssetId,
        GlobalAssetType assetType,
        String displayName,
        Optional<String> description,
        Instant createdAt,
        Map<String, String> metadata
) {
    public GlobalAsset {
        Objects.requireNonNull(globalAssetId, "globalAssetId");
        Objects.requireNonNull(assetType, "assetType");
        displayName = displayName == null || displayName.isBlank() ? globalAssetId.value() : displayName;
        description = description == null ? Optional.empty() : description;
        Objects.requireNonNull(createdAt, "createdAt");
        metadata = MetadataMaps.immutable(metadata);
    }
}
