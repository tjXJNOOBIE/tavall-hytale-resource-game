package org.tavall.control.citizen;

import java.util.Map;

public record CitizenPopulationProjection(
        CitizenSummaryScope scope,
        int totalCitizens,
        int totalTroops,
        int inTraining,
        int wounded,
        Map<String, Integer> moraleSummary,
        Map<String, Integer> healthSummary,
        Map<String, Integer> jobSummary,
        Map<String, Integer> ageStageSummary,
        String globalAssetId,
        Map<String, String> metadata
) {
}
