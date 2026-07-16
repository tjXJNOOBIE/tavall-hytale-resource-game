package org.tavall.control.asset;

import org.tavall.control.common.GamePlatform;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryGlobalAssetRepository implements GlobalAssetRepository {
    private final Map<GlobalAssetId, GlobalAsset> assetsById = new ConcurrentHashMap<>();
    private final Map<String, PlatformAssetVersion> activeVersionsByAssetPlatform = new ConcurrentHashMap<>();

    @Override
    public GlobalAsset saveGlobalAsset(GlobalAsset globalAsset) {
        GlobalAsset existing = assetsById.putIfAbsent(globalAsset.globalAssetId(), globalAsset);
        if (existing != null) {
            throw new AssetOperationException("Global asset already exists: " + globalAsset.globalAssetId());
        }
        return globalAsset;
    }

    @Override
    public Optional<GlobalAsset> findGlobalAsset(GlobalAssetId globalAssetId) {
        return Optional.ofNullable(assetsById.get(globalAssetId));
    }

    @Override
    public PlatformAssetVersion savePlatformAssetVersion(PlatformAssetVersion platformAssetVersion) {
        if (platformAssetVersion.active()) {
            String key = activeVersionKey(platformAssetVersion.globalAssetId(), platformAssetVersion.platform());
            PlatformAssetVersion existing = activeVersionsByAssetPlatform.putIfAbsent(key, platformAssetVersion);
            if (existing != null && existing.version() != platformAssetVersion.version()) {
                throw new AssetOperationException("Active platform asset version already exists for " + key + ".");
            }
            activeVersionsByAssetPlatform.put(key, platformAssetVersion);
        }
        return platformAssetVersion;
    }

    @Override
    public Optional<PlatformAssetVersion> findActivePlatformAssetVersion(GlobalAssetId globalAssetId, GamePlatform platform) {
        return Optional.ofNullable(activeVersionsByAssetPlatform.get(activeVersionKey(globalAssetId, platform)));
    }

    private String activeVersionKey(GlobalAssetId globalAssetId, GamePlatform platform) {
        return globalAssetId.value() + ":" + platform.name();
    }
}
