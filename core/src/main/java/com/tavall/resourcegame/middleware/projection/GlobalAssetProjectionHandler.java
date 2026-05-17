package com.tavall.resourcegame.middleware.projection;

import com.tavall.resourcegame.middleware.asset.GlobalAssetId;
import com.tavall.resourcegame.middleware.asset.GlobalAssetResolutionHandler;
import com.tavall.resourcegame.middleware.asset.ResolvedPlatformAsset;
import com.tavall.resourcegame.middleware.common.GamePlatform;

public final class GlobalAssetProjectionHandler implements IProjectionDomain {
    public GlobalAssetProjectionHandler() {
    }

    public GlobalAssetProjectionHandler(GlobalAssetResolutionHandler globalAssetResolutionHandler) {
        registerGlobalAssetResolutionHandler(globalAssetResolutionHandler);
    }

    public ResolvedPlatformAsset resolveAssetForProjection(GlobalAssetId globalAssetId, GamePlatform platform) {
        return getGlobalAssetResolutionHandler().resolveGlobalAssetForPlatform(globalAssetId, platform);
    }
}
