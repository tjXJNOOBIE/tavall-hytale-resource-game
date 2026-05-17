package com.tavall.resourcegame.middleware.asset;

import com.tavall.resourcegame.middleware.common.GamePlatform;

import java.util.Optional;

public interface GlobalAssetRepository {
    GlobalAsset saveGlobalAsset(GlobalAsset globalAsset);

    Optional<GlobalAsset> findGlobalAsset(GlobalAssetId globalAssetId);

    PlatformAssetVersion savePlatformAssetVersion(PlatformAssetVersion platformAssetVersion);

    Optional<PlatformAssetVersion> findActivePlatformAssetVersion(GlobalAssetId globalAssetId, GamePlatform platform);
}
