package com.tavall.hytale.resourcegame.middleware.citizen;

import java.util.Optional;

public interface CitizenSummaryCacheRepository {
    Optional<CitizenSummaryBundle> findMemorySummary(CitizenSummaryScope scope);

    Optional<CitizenSummaryBundle> findRedisSummary(CitizenSummaryScope scope);

    void saveMemorySummary(CitizenSummaryScope scope, CitizenSummaryBundle summary);

    void saveRedisSummary(CitizenSummaryScope scope, CitizenSummaryBundle summary);

    void invalidateMemorySummary(CitizenSummaryScope scope);

    void markRedisDirty(CitizenSummaryScope scope);

    void clearRedisDirty(CitizenSummaryScope scope);

    boolean redisDirty(CitizenSummaryScope scope);
}
