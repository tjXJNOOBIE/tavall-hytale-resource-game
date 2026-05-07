package com.tavall.hytale.resourcegame.middleware.healing;

import com.tavall.hytale.resourcegame.middleware.event.DomainEventPublisher;
import com.tavall.hytale.resourcegame.middleware.event.SimpleDomainEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class TroopHealingProgressTickHandler {
    private final TroopHealingRepository troopHealingRepository;
    private final TroopHealingCompletionHandler completionHandler;
    private final DomainEventPublisher domainEventPublisher;

    public TroopHealingProgressTickHandler(
            TroopHealingRepository troopHealingRepository,
            TroopHealingCompletionHandler completionHandler,
            DomainEventPublisher domainEventPublisher
    ) {
        this.troopHealingRepository = troopHealingRepository;
        this.completionHandler = completionHandler;
        this.domainEventPublisher = domainEventPublisher;
    }

    public List<TroopHealingPlan> runHealingProgressTick(Instant now) {
        ArrayList<TroopHealingPlan> updatedPlans = new ArrayList<>();
        for (TroopHealingPlan plan : troopHealingRepository.findActiveHealingPlans()) {
            domainEventPublisher.publish(new SimpleDomainEvent(
                    "TroopHealingProgressedEvent",
                    now,
                    Map.of("troopId", plan.troopId().value().toString(), "healingPlanId", plan.healingPlanId().toString())
            ));
            if (!plan.completesAt().isAfter(now)) {
                updatedPlans.add(completionHandler.completeHealingPlan(plan.healingPlanId(), now));
            } else {
                updatedPlans.add(plan);
            }
        }
        return List.copyOf(updatedPlans);
    }
}
