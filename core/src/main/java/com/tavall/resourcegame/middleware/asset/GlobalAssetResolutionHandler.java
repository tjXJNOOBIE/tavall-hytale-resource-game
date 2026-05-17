package com.tavall.resourcegame.middleware.asset;

import com.tavall.resourcegame.middleware.common.GamePlatform;

import java.util.Optional;

public final class GlobalAssetResolutionHandler implements IAssetDomain {
    public GlobalAssetResolutionHandler() {
    }

    public GlobalAssetResolutionHandler(GlobalAssetRepository globalAssetRepository) {
        registerGlobalAssetRepository(globalAssetRepository);
    }

    public ResolvedPlatformAsset resolveGlobalAssetForPlatform(GlobalAssetId globalAssetId, GamePlatform platform) {
        Optional<PlatformAssetVersion> activeVersion = getGlobalAssetRepository().findActivePlatformAssetVersion(globalAssetId, platform);
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
