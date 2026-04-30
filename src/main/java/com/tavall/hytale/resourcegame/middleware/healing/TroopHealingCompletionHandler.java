package com.tavall.hytale.resourcegame.middleware.healing;

import com.tavall.hytale.resourcegame.middleware.event.DomainEventPublisher;
import com.tavall.hytale.resourcegame.middleware.event.SimpleDomainEvent;
import com.tavall.hytale.resourcegame.middleware.troop.Troop;
import com.tavall.hytale.resourcegame.middleware.troop.TroopRepository;
import com.tavall.hytale.resourcegame.middleware.troop.TroopStatus;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public final class TroopHealingCompletionHandler {
    private final TroopRepository troopRepository;
    private final TroopHealingRepository troopHealingRepository;
    private final DomainEventPublisher domainEventPublisher;

    public TroopHealingCompletionHandler(
            TroopRepository troopRepository,
            TroopHealingRepository troopHealingRepository,
            DomainEventPublisher domainEventPublisher
    ) {
        this.troopRepository = troopRepository;
        this.troopHealingRepository = troopHealingRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    public TroopHealingPlan completeHealingPlan(UUID healingPlanId, Instant now) {
        TroopHealingPlan activePlan = troopHealingRepository.findHealingPlan(healingPlanId)
                .orElseThrow(() -> new HealingValidationException("Healing plan was not found."));
        if (activePlan.state() != HealingState.ACTIVE) {
            return activePlan;
        }
        TroopWound wound = troopHealingRepository.findWound(activePlan.woundId())
                .orElseThrow(() -> new HealingValidationException("Healing wound was not found."));
        troopHealingRepository.saveWound(wound.markHealed(now));
        Troop troop = troopRepository.findTroop(activePlan.troopId())
                .orElseThrow(() -> new HealingValidationException("Troop was not found."));
        TroopHealingPlan completedPlan = troopHealingRepository.saveHealingPlan(activePlan.withState(HealingState.COMPLETED));
        if (troop.status() == TroopStatus.WOUNDED && troopHealingRepository.findActiveWoundsForTroop(troop.troopId()).isEmpty()) {
            troopRepository.saveTroop(troop.withStatus(TroopStatus.IDLE));
        }
        domainEventPublisher.publish(new SimpleDomainEvent(
                "TroopHealingCompletedEvent",
                now,
                Map.of("troopId", troop.troopId().value().toString(), "healingPlanId", healingPlanId.toString(), "woundId", wound.woundId().toString())
        ));
        return completedPlan;
    }
}
