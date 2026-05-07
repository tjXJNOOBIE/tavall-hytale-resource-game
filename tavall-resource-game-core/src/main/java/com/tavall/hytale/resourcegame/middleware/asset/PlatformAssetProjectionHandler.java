package com.tavall.hytale.resourcegame.middleware.asset;

import com.tavall.hytale.resourcegame.middleware.common.GamePlatform;

public final class PlatformAssetProjectionHandler {
    private final GlobalAssetResolutionHandler globalAssetResolutionHandler;

    public PlatformAssetProjectionHandler(GlobalAssetResolutionHandler globalAssetResolutionHandler) {
        this.globalAssetResolutionHandler = globalAssetResolutionHandler;
    }

    public ResolvedPlatformAsset projectGlobalAsset(GlobalAssetId globalAssetId, GamePlatform platform) {
        return globalAssetResolutionHandler.resolveGlobalAssetForPlatform(globalAssetId, platform);
    }
}
