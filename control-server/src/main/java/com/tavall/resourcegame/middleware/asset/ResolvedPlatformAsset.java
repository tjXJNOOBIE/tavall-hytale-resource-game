package com.tavall.resourcegame.middleware.asset;

import com.tavall.resourcegame.middleware.common.GamePlatform;

import java.util.Optional;

public record ResolvedPlatformAsset(
        GlobalAssetId globalAssetId,
        GamePlatform platform,
        Optional<String> platformAssetReference,
        String fallbackAssetKey
) {
    public ResolvedPlatformAsset {
        platformAssetReference = platformAssetReference == null ? Optional.empty() : platformAssetReference;
        fallbackAssetKey = fallbackAssetKey == null || fallbackAssetKey.isBlank()
                ? globalAssetId.value()
                : fallbackAssetKey;
    }
}
