package com.tavall.resourcegame.middleware.asset;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public final class GlobalAssetRegistrationHandler implements IAssetDomain {
    public GlobalAssetRegistrationHandler() {
    }

    public GlobalAssetRegistrationHandler(GlobalAssetRepository globalAssetRepository) {
        registerGlobalAssetRepository(globalAssetRepository);
    }

    public GlobalAsset registerGlobalAsset(
            GlobalAssetId globalAssetId,
            GlobalAssetType assetType,
            String displayName,
            Optional<String> description,
            Instant now
    ) {
        return getGlobalAssetRepository().saveGlobalAsset(new GlobalAsset(globalAssetId, assetType, displayName, description, now, Map.of()));
    }
}
