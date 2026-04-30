package com.tavall.hytale.resourcegame.middleware.asset;

import com.tavall.hytale.resourcegame.middleware.common.GamePlatform;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class PlatformAssetVersionRegistrationHandler {
    private final GlobalAssetRepository globalAssetRepository;

    public PlatformAssetVersionRegistrationHandler(GlobalAssetRepository globalAssetRepository) {
        this.globalAssetRepository = globalAssetRepository;
    }

    public PlatformAssetVersion registerPlatformAssetVersion(
            GlobalAssetId globalAssetId,
            GamePlatform platform,
            String assetReference,
            int version,
            Optional<String> contentHash,
            Instant now
    ) {
        if (globalAssetRepository.findGlobalAsset(globalAssetId).isEmpty()) {
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
        return globalAssetRepository.savePlatformAssetVersion(platformAssetVersion);
    }
}
