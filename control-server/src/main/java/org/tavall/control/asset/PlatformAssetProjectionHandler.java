package org.tavall.control.asset;

import org.tavall.control.common.GamePlatform;

public final class PlatformAssetProjectionHandler implements AssetDomain {
    public PlatformAssetProjectionHandler() {
    }

    public PlatformAssetProjectionHandler(GlobalAssetResolutionHandler globalAssetResolutionHandler) {
        registerGlobalAssetResolutionHandler(globalAssetResolutionHandler);
    }

    public ResolvedPlatformAsset projectGlobalAsset(GlobalAssetId globalAssetId, GamePlatform platform) {
        return getGlobalAssetResolutionHandler().resolveGlobalAssetForPlatform(globalAssetId, platform);
    }
}
