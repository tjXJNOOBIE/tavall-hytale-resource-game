package com.tavall.resourcegame.middleware.citizen;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;

import java.util.Optional;

public interface CitizenSummaryCacheRepository extends IDependencyInjectableInterface {
    Optional<CitizenSummaryBundle> findMemorySummary(CitizenSummaryScope scope);

    Optional<CitizenSummaryBundle> findRedisSummary(CitizenSummaryScope scope);

    void saveMemorySummary(CitizenSummaryScope scope, CitizenSummaryBundle summary);

    void saveRedisSummary(CitizenSummaryScope scope, CitizenSummaryBundle summary);

    void invalidateMemorySummary(CitizenSummaryScope scope);

    void markRedisDirty(CitizenSummaryScope scope);

    void clearRedisDirty(CitizenSummaryScope scope);

    boolean redisDirty(CitizenSummaryScope scope);
}
