package com.tavall.hytale.resourcegame.middleware.asset;

import com.tavall.hytale.resourcegame.middleware.common.GamePlatform;

import java.util.Optional;

public final class GlobalAssetResolutionHandler {
    private final GlobalAssetRepository globalAssetRepository;

    public GlobalAssetResolutionHandler(GlobalAssetRepository globalAssetRepository) {
        this.globalAssetRepository = globalAssetRepository;
    }

    public ResolvedPlatformAsset resolveGlobalAssetForPlatform(GlobalAssetId globalAssetId, GamePlatform platform) {
        Optional<PlatformAssetVersion> activeVersion = globalAssetRepository.findActivePlatformAssetVersion(globalAssetId, platform);
        return new ResolvedPlatformAsset(
                globalAssetId,
                platform,
                activeVersion.map(PlatformAssetVersion::assetReference),
                fallbackAssetKey(globalAssetId, platform)
        );
    }

    private String fallbackAssetKey(GlobalAssetId globalAssetId, GamePlatform platform) {
        return "fallback:" + platform.name().toLowerCase() + ":" + globalAssetId.value();
    }
}
