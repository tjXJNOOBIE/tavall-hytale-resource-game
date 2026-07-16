package org.tavall.control.healing;

import org.tavall.control.troop.TroopId;

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
