package org.tavall.control.projection;

import org.tavall.control.asset.GlobalAssetId;
import org.tavall.control.asset.GlobalAssetResolutionHandler;
import org.tavall.control.asset.ResolvedPlatformAsset;
import org.tavall.control.common.GamePlatform;

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
