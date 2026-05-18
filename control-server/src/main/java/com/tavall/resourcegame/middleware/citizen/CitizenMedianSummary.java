package org.tavall.control.citizen;

import java.time.Instant;

public record CitizenMedianSummary(
        CitizenSummaryScope scope,
        double medianStrength,
        double medianEndurance,
        double medianAgility,
        double medianDiscipline,
        double medianIntelligence,
        double medianMoraleResilience,
        double medianWorkEfficiency,
        double medianCombatPotential,
        Instant updatedAt
) {
}
