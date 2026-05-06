package com.tavall.hytale.resourcegame.middleware.citizen;

import java.time.Instant;
import java.util.List;

public final class CitizenSummaryCacheRefreshHandler {
    private final CitizenRepository citizenRepository;
    private final CitizenSummaryCacheRepository cacheRepository;
    private final CitizenAggregationCalculationHandler aggregationCalculationHandler;

    public CitizenSummaryCacheRefreshHandler(
            CitizenRepository citizenRepository,
            CitizenSummaryCacheRepository cacheRepository,
            CitizenAggregationCalculationHandler aggregationCalculationHandler
    ) {
        this.citizenRepository = citizenRepository;
        this.cacheRepository = cacheRepository;
        this.aggregationCalculationHandler = aggregationCalculationHandler;
    }

    public CitizenSummaryBundle readSummary(CitizenSummaryScope scope, Instant now) {
        return cacheRepository.findMemorySummary(scope)
                .or(() -> cacheRepository.findRedisSummary(scope)
                        .map(summary -> {
                            cacheRepository.saveMemorySummary(scope, summary);
                            return summary;
                        }))
                .orElseGet(() -> refreshSummary(scope, now));
    }

    public CitizenSummaryBundle refreshSummary(CitizenSummaryScope scope, Instant now) {
        List<CitizenData> citizens = switch (scope.scopeType()) {
            case PLAYER -> citizenRepository.findCitizensForPlayer(com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId.of(java.util.UUID.fromString(scope.scopeId())));
            case KINGDOM -> citizenRepository.findCitizensForKingdom(scope.scopeId());
        };
        CitizenSummaryBundle summary = aggregationCalculationHandler.calculate(scope, citizens, now);
        cacheRepository.saveRedisSummary(scope, summary);
        cacheRepository.saveMemorySummary(scope, summary);
        return summary;
    }
}
