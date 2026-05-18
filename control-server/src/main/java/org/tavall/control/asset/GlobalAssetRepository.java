package org.tavall.control.asset;

import org.tavall.control.common.GamePlatform;

import java.util.Optional;

public interface GlobalAssetRepository {
    GlobalAsset saveGlobalAsset(GlobalAsset globalAsset);

    Optional<GlobalAsset> findGlobalAsset(GlobalAssetId globalAssetId);

    PlatformAssetVersion savePlatformAssetVersion(PlatformAssetVersion platformAssetVersion);

    Optional<PlatformAssetVersion> findActivePlatformAssetVersion(GlobalAssetId globalAssetId, GamePlatform platform);
}
