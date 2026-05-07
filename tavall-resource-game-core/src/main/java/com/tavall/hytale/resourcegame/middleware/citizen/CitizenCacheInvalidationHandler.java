package com.tavall.hytale.resourcegame.middleware.citizen;

public final class CitizenCacheInvalidationHandler {
    private final CitizenSummaryCacheRepository cacheRepository;

    public CitizenCacheInvalidationHandler(CitizenSummaryCacheRepository cacheRepository) {
        this.cacheRepository = cacheRepository;
    }

    public void invalidateCitizenScopes(CitizenData citizen) {
        invalidate(CitizenSummaryScope.player(citizen.ownerPlayerId()));
        invalidate(CitizenSummaryScope.kingdom(citizen.kingdomId()));
    }

    public void invalidate(CitizenSummaryScope scope) {
        cacheRepository.invalidateMemorySummary(scope);
        cacheRepository.markRedisDirty(scope);
    }
}
