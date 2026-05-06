package com.tavall.hytale.resourcegame.middleware.citizen;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryCitizenSummaryCacheRepository implements CitizenSummaryCacheRepository {
    private final ConcurrentMap<CitizenSummaryScope, CitizenSummaryBundle> memorySummaries = new ConcurrentHashMap<>();
    private final ConcurrentMap<CitizenSummaryScope, CitizenSummaryBundle> redisLikeSummaries = new ConcurrentHashMap<>();
    private final Set<CitizenSummaryScope> redisDirtyScopes = ConcurrentHashMap.newKeySet();

    @Override
    public Optional<CitizenSummaryBundle> findMemorySummary(CitizenSummaryScope scope) {
        return Optional.ofNullable(memorySummaries.get(scope));
    }

    @Override
    public Optional<CitizenSummaryBundle> findRedisSummary(CitizenSummaryScope scope) {
        if (redisDirty(scope)) {
            return Optional.empty();
        }
        return Optional.ofNullable(redisLikeSummaries.get(scope));
    }

    @Override
    public void saveMemorySummary(CitizenSummaryScope scope, CitizenSummaryBundle summary) {
        memorySummaries.put(scope, summary);
    }

    @Override
    public void saveRedisSummary(CitizenSummaryScope scope, CitizenSummaryBundle summary) {
        redisLikeSummaries.put(scope, summary);
        clearRedisDirty(scope);
    }

    @Override
    public void invalidateMemorySummary(CitizenSummaryScope scope) {
        memorySummaries.remove(scope);
    }

    @Override
    public void markRedisDirty(CitizenSummaryScope scope) {
        redisDirtyScopes.add(scope);
    }

    @Override
    public void clearRedisDirty(CitizenSummaryScope scope) {
        redisDirtyScopes.remove(scope);
    }

    @Override
    public boolean redisDirty(CitizenSummaryScope scope) {
        return redisDirtyScopes.contains(scope);
    }
}
