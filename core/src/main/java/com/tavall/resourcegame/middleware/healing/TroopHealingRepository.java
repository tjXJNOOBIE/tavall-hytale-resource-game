package com.tavall.resourcegame.middleware.healing;

import com.tavall.resourcegame.middleware.troop.TroopId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TroopHealingRepository {
    TroopWound saveWound(TroopWound wound);

    Optional<TroopWound> findWound(UUID woundId);

    List<TroopWound> findActiveWoundsForTroop(TroopId troopId);

    TroopHealingPlan saveHealingPlan(TroopHealingPlan healingPlan);

    Optional<TroopHealingPlan> findHealingPlan(UUID healingPlanId);

    Optional<TroopHealingPlan> findActiveHealingPlanForTroop(TroopId troopId);

    List<TroopHealingPlan> findActiveHealingPlans();
}
