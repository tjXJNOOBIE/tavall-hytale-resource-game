package org.tavall.control.citizen;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.time.Instant;
import java.util.List;

public final class CitizenSummaryCacheRefreshHandler implements ICitizenDomain, IDependencyInjectableConcrete {
    public CitizenSummaryCacheRefreshHandler() {
    }

    public CitizenSummaryCacheRefreshHandler(
            CitizenRepository citizenRepository,
            CitizenSummaryCacheRepository cacheRepository,
            CitizenAggregationCalculationHandler aggregationCalculationHandler
    ) {
        registerCitizenDependency(CitizenRepository.class, citizenRepository);
        registerCitizenDependency(CitizenSummaryCacheRepository.class, cacheRepository);
        registerCitizenDependency(CitizenAggregationCalculationHandler.class, aggregationCalculationHandler);
    }

    public CitizenSummaryBundle readSummary(CitizenSummaryScope scope, Instant now) {
        return getCitizenSummaryCacheRepository().findMemorySummary(scope)
                .or(() -> getCitizenSummaryCacheRepository().findRedisSummary(scope)
                        .map(summary -> {
                            getCitizenSummaryCacheRepository().saveMemorySummary(scope, summary);
                            return summary;
                        }))
                .orElseGet(() -> refreshSummary(scope, now));
    }

    public CitizenSummaryBundle refreshSummary(CitizenSummaryScope scope, Instant now) {
        List<CitizenData> citizens = switch (scope.scopeType()) {
            case PLAYER -> getCitizenRepository().findCitizensForPlayer(org.tavall.control.identity.UniversalPlayerId.of(java.util.UUID.fromString(scope.scopeId())));
            case KINGDOM -> getCitizenRepository().findCitizensForKingdom(scope.scopeId());
        };
        CitizenSummaryBundle summary = getCitizenAggregationCalculationHandler().calculate(scope, citizens, now);
        getCitizenSummaryCacheRepository().saveRedisSummary(scope, summary);
        getCitizenSummaryCacheRepository().saveMemorySummary(scope, summary);
        return summary;
    }
}
