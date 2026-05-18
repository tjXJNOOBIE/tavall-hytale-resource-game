package org.tavall.control.healing;

import org.tavall.control.event.DomainEventPublisher;
import org.tavall.control.event.SimpleDomainEvent;
import org.tavall.control.troop.Troop;
import org.tavall.control.troop.TroopRepository;
import org.tavall.control.troop.TroopStatus;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public final class TroopHealingCompletionHandler implements IHealingDomain {
    public TroopHealingCompletionHandler() {
    }

    public TroopHealingCompletionHandler(
            TroopRepository troopRepository,
            TroopHealingRepository troopHealingRepository,
            DomainEventPublisher domainEventPublisher
    ) {
        registerTroopRepository(troopRepository);
        registerTroopHealingRepository(troopHealingRepository);
        registerDomainEventPublisher(domainEventPublisher);
    }

    public TroopHealingPlan completeHealingPlan(UUID healingPlanId, Instant now) {
        TroopHealingPlan activePlan = getTroopHealingRepository().findHealingPlan(healingPlanId)
                .orElseThrow(() -> new HealingValidationException("Healing plan was not found."));
        if (activePlan.state() != HealingState.ACTIVE) {
            return activePlan;
        }
        TroopWound wound = getTroopHealingRepository().findWound(activePlan.woundId())
                .orElseThrow(() -> new HealingValidationException("Healing wound was not found."));
        getTroopHealingRepository().saveWound(wound.markHealed(now));
        Troop troop = getTroopRepository().findTroop(activePlan.troopId())
                .orElseThrow(() -> new HealingValidationException("Troop was not found."));
        TroopHealingPlan completedPlan = getTroopHealingRepository().saveHealingPlan(activePlan.withState(HealingState.COMPLETED));
        if (troop.status() == TroopStatus.WOUNDED && getTroopHealingRepository().findActiveWoundsForTroop(troop.troopId()).isEmpty()) {
            getTroopRepository().saveTroop(troop.withStatus(TroopStatus.IDLE));
        }
        getDomainEventPublisher().publish(new SimpleDomainEvent(
                "TroopHealingCompletedEvent",
                now,
                Map.of("troopId", troop.troopId().value().toString(), "healingPlanId", healingPlanId.toString(), "woundId", wound.woundId().toString())
        ));
        return completedPlan;
    }
}
