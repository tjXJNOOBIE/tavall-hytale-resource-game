package com.tavall.resourcegame.middleware.healing;

import com.tavall.resourcegame.middleware.troop.TroopId;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryTroopHealingRepository implements TroopHealingRepository {
    private final Map<UUID, TroopWound> woundsById = new ConcurrentHashMap<>();
    private final Map<UUID, TroopHealingPlan> healingPlansById = new ConcurrentHashMap<>();

    @Override
    public TroopWound saveWound(TroopWound wound) {
        woundsById.put(wound.woundId(), wound);
        return wound;
    }

    @Override
    public Optional<TroopWound> findWound(UUID woundId) {
        return Optional.ofNullable(woundsById.get(woundId));
    }

    @Override
    public List<TroopWound> findActiveWoundsForTroop(TroopId troopId) {
        return woundsById.values().stream()
                .filter(wound -> wound.troopId().equals(troopId))
                .filter(TroopWound::active)
                .sorted(Comparator.comparing(TroopWound::createdAt))
                .toList();
    }

    @Override
    public TroopHealingPlan saveHealingPlan(TroopHealingPlan healingPlan) {
        healingPlansById.put(healingPlan.healingPlanId(), healingPlan);
        return healingPlan;
    }

    @Override
    public Optional<TroopHealingPlan> findHealingPlan(UUID healingPlanId) {
        return Optional.ofNullable(healingPlansById.get(healingPlanId));
    }

    @Override
    public Optional<TroopHealingPlan> findActiveHealingPlanForTroop(TroopId troopId) {
        return healingPlansById.values().stream()
                .filter(plan -> plan.troopId().equals(troopId))
                .filter(plan -> plan.state() == HealingState.ACTIVE || plan.state() == HealingState.PENDING)
                .min(Comparator.comparing(TroopHealingPlan::startedAt));
    }

    @Override
    public List<TroopHealingPlan> findActiveHealingPlans() {
        return healingPlansById.values().stream()
                .filter(plan -> plan.state() == HealingState.ACTIVE)
                .sorted(Comparator.comparing(TroopHealingPlan::startedAt))
                .toList();
    }
}
