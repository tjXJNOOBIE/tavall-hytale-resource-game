package org.tavall.control.asset;

import org.tavall.control.common.GamePlatform;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class PlatformAssetVersionRegistrationHandler implements AssetDomain {
    public PlatformAssetVersionRegistrationHandler() {
    }

    public PlatformAssetVersionRegistrationHandler(GlobalAssetRepository globalAssetRepository) {
        registerGlobalAssetRepository(globalAssetRepository);
    }

    public PlatformAssetVersion registerPlatformAssetVersion(
            GlobalAssetId globalAssetId,
            GamePlatform platform,
            String assetReference,
            int version,
            Optional<String> contentHash,
            Instant now
    ) {
        if (getGlobalAssetRepository().findGlobalAsset(globalAssetId).isEmpty()) {
            throw new AssetOperationException("Global asset must be registered before platform versions.");
        }
        PlatformAssetVersion platformAssetVersion = new PlatformAssetVersion(
                UUID.randomUUID(),
                globalAssetId,
                platform,
                assetReference,
                version,
                contentHash,
                true,
                now,
                Map.of()
        );
        return getGlobalAssetRepository().savePlatformAssetVersion(platformAssetVersion);
    }
}
