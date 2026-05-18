package org.tavall.control.healing;

import org.tavall.control.event.DomainEventPublisher;
import org.tavall.control.event.SimpleDomainEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class TroopHealingProgressTickHandler implements IHealingDomain {
    public TroopHealingProgressTickHandler() {
    }

    public TroopHealingProgressTickHandler(
            TroopHealingRepository troopHealingRepository,
            TroopHealingCompletionHandler completionHandler,
            DomainEventPublisher domainEventPublisher
    ) {
        registerTroopHealingRepository(troopHealingRepository);
        registerTroopHealingCompletionHandler(completionHandler);
        registerDomainEventPublisher(domainEventPublisher);
    }

    public List<TroopHealingPlan> runHealingProgressTick(Instant now) {
        ArrayList<TroopHealingPlan> updatedPlans = new ArrayList<>();
        for (TroopHealingPlan plan : getTroopHealingRepository().findActiveHealingPlans()) {
            getDomainEventPublisher().publish(new SimpleDomainEvent(
                    "TroopHealingProgressedEvent",
                    now,
                    Map.of("troopId", plan.troopId().value().toString(), "healingPlanId", plan.healingPlanId().toString())
            ));
            if (!plan.completesAt().isAfter(now)) {
                updatedPlans.add(getTroopHealingCompletionHandler().completeHealingPlan(plan.healingPlanId(), now));
            } else {
                updatedPlans.add(plan);
            }
        }
        return List.copyOf(updatedPlans);
    }
}
