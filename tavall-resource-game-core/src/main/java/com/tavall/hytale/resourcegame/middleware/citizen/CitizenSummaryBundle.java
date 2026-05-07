package com.tavall.hytale.resourcegame.middleware.citizen;

public record CitizenSummaryBundle(
        CitizenPopulationSummary populationSummary,
        CitizenMedianSummary medianSummary,
        CitizenProductivitySummary productivitySummary
) {
}
