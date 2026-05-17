package com.tavall.resourcegame.middleware.asset;

import com.tavall.resourcegame.middleware.common.GamePlatform;

public final class PlatformAssetProjectionHandler implements IAssetDomain {
    public PlatformAssetProjectionHandler() {
    }

    public PlatformAssetProjectionHandler(GlobalAssetResolutionHandler globalAssetResolutionHandler) {
        registerGlobalAssetResolutionHandler(globalAssetResolutionHandler);
    }

    public ResolvedPlatformAsset projectGlobalAsset(GlobalAssetId globalAssetId, GamePlatform platform) {
        return getGlobalAssetResolutionHandler().resolveGlobalAssetForPlatform(globalAssetId, platform);
    }
}
