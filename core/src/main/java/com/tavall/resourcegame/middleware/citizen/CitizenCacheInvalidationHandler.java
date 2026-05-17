package com.tavall.resourcegame.middleware.citizen;

import com.tavall.resourcegame.dependency.IDependencyInjectableConcrete;

public final class CitizenCacheInvalidationHandler implements ICitizenDomain, IDependencyInjectableConcrete {
    public CitizenCacheInvalidationHandler() {
    }

    public CitizenCacheInvalidationHandler(CitizenSummaryCacheRepository cacheRepository) {
        registerCitizenDependency(CitizenSummaryCacheRepository.class, cacheRepository);
    }

    public void invalidateCitizenScopes(CitizenData citizen) {
        invalidate(CitizenSummaryScope.player(citizen.ownerPlayerId()));
        invalidate(CitizenSummaryScope.kingdom(citizen.kingdomId()));
    }

    public void invalidate(CitizenSummaryScope scope) {
        getCitizenSummaryCacheRepository().invalidateMemorySummary(scope);
        getCitizenSummaryCacheRepository().markRedisDirty(scope);
    }
}
