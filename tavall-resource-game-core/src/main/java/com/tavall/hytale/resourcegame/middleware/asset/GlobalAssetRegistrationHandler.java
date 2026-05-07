package com.tavall.hytale.resourcegame.middleware.asset;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public final class GlobalAssetRegistrationHandler {
    private final GlobalAssetRepository globalAssetRepository;

    public GlobalAssetRegistrationHandler(GlobalAssetRepository globalAssetRepository) {
        this.globalAssetRepository = globalAssetRepository;
    }

    public GlobalAsset registerGlobalAsset(
            GlobalAssetId globalAssetId,
            GlobalAssetType assetType,
            String displayName,
            Optional<String> description,
            Instant now
    ) {
        return globalAssetRepository.saveGlobalAsset(new GlobalAsset(globalAssetId, assetType, displayName, description, now, Map.of()));
    }
}
