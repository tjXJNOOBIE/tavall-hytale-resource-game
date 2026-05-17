package com.tavall.resourcegame.middleware.citizen;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import com.tavall.resourcegame.domain.CitizenJobType;

import java.time.Instant;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.function.ToIntFunction;

public final class CitizenAggregationCalculationHandler implements ICitizenDomain, IDependencyInjectableConcrete {
    public CitizenAggregationCalculationHandler() {
    }

    public CitizenAggregationCalculationHandler(
            CitizenFoodEffectHandler foodEffectHandler,
            CitizenMoraleEffectHandler moraleEffectHandler,
            CitizenHousingEffectHandler housingEffectHandler,
            CitizenClockIntegrationHandler clockIntegrationHandler
    ) {
        registerCitizenDependency(CitizenFoodEffectHandler.class, foodEffectHandler);
        registerCitizenDependency(CitizenMoraleEffectHandler.class, moraleEffectHandler);
        registerCitizenDependency(CitizenHousingEffectHandler.class, housingEffectHandler);
        registerCitizenDependency(CitizenClockIntegrationHandler.class, clockIntegrationHandler);
    }

    public CitizenSummaryBundle calculate(CitizenSummaryScope scope, List<CitizenData> citizens, Instant now) {
        CitizenPopulationSummary populationSummary = populationSummary(scope, citizens, now);
        CitizenMedianSummary medianSummary = medianSummary(scope, citizens, now);
        CitizenProductivitySummary productivitySummary = productivitySummary(scope, citizens, now);
        return new CitizenSummaryBundle(populationSummary, medianSummary, productivitySummary);
    }

    private CitizenPopulationSummary populationSummary(CitizenSummaryScope scope, List<CitizenData> citizens, Instant now) {
        EnumMap<CitizenAgeStage, Integer> ageCounts = new EnumMap<>(CitizenAgeStage.class);
        EnumMap<CitizenJobType, Integer> jobCounts = new EnumMap<>(CitizenJobType.class);
        EnumMap<CitizenMoraleState, Integer> moraleCounts = new EnumMap<>(CitizenMoraleState.class);
        EnumMap<CitizenHealthState, Integer> healthCounts = new EnumMap<>(CitizenHealthState.class);
        EnumMap<CitizenHousingState, Integer> housingCounts = new EnumMap<>(CitizenHousingState.class);
        EnumMap<CitizenNutritionState, Integer> nutritionCounts = new EnumMap<>(CitizenNutritionState.class);
        citizens.forEach(citizen -> {
            increment(ageCounts, citizen.ageStage());
            increment(jobCounts, citizen.jobType());
            increment(moraleCounts, citizen.moraleState());
            increment(healthCounts, citizen.healthState());
            increment(housingCounts, citizen.housingState());
            increment(nutritionCounts, citizen.nutritionState());
        });
        int activeTroops = count(citizens, CitizenStatus.ACTIVE_TROOP);
        return new CitizenPopulationSummary(
                scope,
                citizens.size(),
                count(citizens, CitizenStatus.ACTIVE_CITIZEN),
                activeTroops,
                count(citizens, CitizenStatus.IN_TRAINING),
                count(citizens, CitizenStatus.WOUNDED),
                count(citizens, CitizenStatus.RECOVERING),
                count(citizens, CitizenStatus.RETIRED),
                count(citizens, CitizenStatus.DEAD),
                ageCounts,
                jobCounts,
                moraleCounts,
                healthCounts,
                housingCounts,
                nutritionCounts,
                now
        );
    }

    private CitizenMedianSummary medianSummary(CitizenSummaryScope scope, List<CitizenData> citizens, Instant now) {
        return new CitizenMedianSummary(
                scope,
                median(citizens, citizen -> citizen.statBlock().strength()),
                median(citizens, citizen -> citizen.statBlock().endurance()),
                median(citizens, citizen -> citizen.statBlock().agility()),
                median(citizens, citizen -> citizen.statBlock().discipline()),
                median(citizens, citizen -> citizen.statBlock().intelligence()),
                median(citizens, citizen -> citizen.statBlock().moraleResilience()),
                median(citizens, citizen -> citizen.statBlock().workEfficiency()),
                median(citizens, citizen -> citizen.statBlock().combatPotential()),
                now
        );
    }

    private CitizenProductivitySummary productivitySummary(CitizenSummaryScope scope, List<CitizenData> citizens, Instant now) {
        double totalWorkPotential = citizens.stream().mapToDouble(citizen -> citizen.statBlock().workEfficiency()).sum();
        double totalCombatPotential = citizens.stream().mapToDouble(citizen -> citizen.statBlock().combatPotential()).sum();
        double moraleModifier = citizens.isEmpty() ? 1.0 : citizens.stream().mapToDouble(citizen -> getCitizenMoraleEffectHandler().modifier(citizen.moraleState())).average().orElse(1.0);
        double foodModifier = citizens.isEmpty() ? 1.0 : citizens.stream().mapToDouble(citizen -> getCitizenFoodEffectHandler().evaluate(citizen).workEfficiencyModifier()).average().orElse(1.0);
        double housingModifier = citizens.isEmpty() ? 1.0 : citizens.stream().mapToDouble(citizen -> getCitizenHousingEffectHandler().modifier(citizen.housingState())).average().orElse(1.0);
        String kingdomId = citizens.stream().findFirst().map(CitizenData::kingdomId).orElse("kingdom-1");
        double clockPhaseModifier = getCitizenClockIntegrationHandler().productivityModifier(kingdomId);
        double effectiveProductivity = totalWorkPotential * moraleModifier * foodModifier * housingModifier * clockPhaseModifier;
        return new CitizenProductivitySummary(scope, totalWorkPotential, totalCombatPotential, effectiveProductivity, moraleModifier, foodModifier, housingModifier, clockPhaseModifier, now);
    }

    private <E extends Enum<E>> void increment(EnumMap<E, Integer> counts, E key) {
        counts.put(key, counts.getOrDefault(key, 0) + 1);
    }

    private int count(List<CitizenData> citizens, CitizenStatus status) {
        return (int) citizens.stream().filter(citizen -> citizen.status() == status).count();
    }

    private double median(List<CitizenData> citizens, ToIntFunction<CitizenData> extractor) {
        if (citizens.isEmpty()) {
            return 0.0;
        }
        List<Integer> values = citizens.stream().mapToInt(extractor).boxed().sorted(Comparator.naturalOrder()).toList();
        int middle = values.size() / 2;
        if (values.size() % 2 == 1) {
            return values.get(middle);
        }
        return (values.get(middle - 1) + values.get(middle)) / 2.0;
    }
}
