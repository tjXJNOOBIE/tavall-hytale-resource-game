package com.tavall.resourcegame.middleware.citizen;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.util.Map;
import java.util.stream.Collectors;

public final class CitizenProjectionHandler implements ICitizenDomain, IDependencyInjectableConcrete {
    public CitizenPopulationProjection projectPopulation(CitizenSummaryBundle summary) {
        CitizenPopulationSummary population = summary.populationSummary();
        return new CitizenPopulationProjection(
                population.scope(),
                population.activeCitizens(),
                population.activeTroops(),
                population.inTraining(),
                population.wounded(),
                enumCounts(population.moraleCounts()),
                enumCounts(population.healthCounts()),
                enumCounts(population.jobCounts()),
                enumCounts(population.ageStageCounts()),
                "citizen.anchor.population",
                Map.of(
                        "canonicalOwner", "plain-java-control-server",
                        "effectiveProductivity", Double.toString(summary.productivitySummary().effectiveProductivity())
                )
        );
    }

    public CitizenDisplayAnchorProjection projectAnchor(CitizenSummaryBundle summary, CitizenAnchorType anchorType) {
        CitizenPopulationSummary population = summary.populationSummary();
        int count = switch (anchorType) {
            case CITIZEN_COUNT -> population.activeCitizens();
            case TROOP_COUNT -> population.activeTroops();
            case WOUNDED_COUNT -> population.wounded();
            case TRAINING_COUNT -> population.inTraining();
        };
        String label = switch (anchorType) {
            case CITIZEN_COUNT -> "Citizens: ";
            case TROOP_COUNT -> "Troops: ";
            case WOUNDED_COUNT -> "Wounded: ";
            case TRAINING_COUNT -> "Training: ";
        };
        String assetId = switch (anchorType) {
            case CITIZEN_COUNT -> "citizen.anchor.population";
            case TROOP_COUNT -> "citizen.anchor.troops";
            case WOUNDED_COUNT -> "citizen.status.wounded";
            case TRAINING_COUNT -> "citizen.visual.training";
        };
        return new CitizenDisplayAnchorProjection(anchorType.name().toLowerCase() + ":" + population.scope().cacheKey(), population.scope(), anchorType, label + count, count, assetId, Map.of("canonicalOwner", "plain-java-control-server"));
    }

    private <E extends Enum<E>> Map<String, Integer> enumCounts(Map<E, Integer> counts) {
        return counts.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().name(), Map.Entry::getValue));
    }
}
