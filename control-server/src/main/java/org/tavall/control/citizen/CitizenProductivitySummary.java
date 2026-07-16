package org.tavall.control.citizen;

import java.time.Instant;

public record CitizenProductivitySummary(
        CitizenSummaryScope scope,
        double totalWorkPotential,
        double totalCombatPotential,
        double effectiveProductivity,
        double moraleModifier,
        double foodModifier,
        double housingModifier,
        double clockPhaseModifier,
        Instant updatedAt
) {
}
