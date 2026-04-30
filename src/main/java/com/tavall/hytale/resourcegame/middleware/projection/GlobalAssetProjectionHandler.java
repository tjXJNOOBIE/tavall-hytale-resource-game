package com.tavall.hytale.resourcegame.middleware.projection;

import com.tavall.hytale.resourcegame.middleware.asset.GlobalAssetId;
import com.tavall.hytale.resourcegame.middleware.asset.GlobalAssetResolutionHandler;
import com.tavall.hytale.resourcegame.middleware.asset.ResolvedPlatformAsset;
import com.tavall.hytale.resourcegame.middleware.common.GamePlatform;

public final class GlobalAssetProjectionHandler {
    private final GlobalAssetResolutionHandler globalAssetResolutionHandler;

    public GlobalAssetProjectionHandler(GlobalAssetResolutionHandler globalAssetResolutionHandler) {
        this.globalAssetResolutionHandler = globalAssetResolutionHandler;
    }

    public ResolvedPlatformAsset resolveAssetForProjection(GlobalAssetId globalAssetId, GamePlatform platform) {
        return globalAssetResolutionHandler.resolveGlobalAssetForPlatform(globalAssetId, platform);
    }
}
