package org.tavall.control.healing;

import org.tavall.control.event.DomainEventPublisher;
import org.tavall.control.event.SimpleDomainEvent;
import org.tavall.control.troop.Troop;
import org.tavall.control.troop.TroopId;
import org.tavall.control.troop.TroopRepository;
import org.tavall.control.troop.TroopStatus;

import java.time.Instant;
import java.util.Map;

public final class TroopWoundAssignmentHandler implements IHealingDomain {
    public TroopWoundAssignmentHandler() {
    }

    public TroopWoundAssignmentHandler(
            TroopRepository troopRepository,
            TroopHealingRepository troopHealingRepository,
            DomainEventPublisher domainEventPublisher
    ) {
        registerTroopRepository(troopRepository);
        registerTroopHealingRepository(troopHealingRepository);
        registerDomainEventPublisher(domainEventPublisher);
    }

    public TroopWound assignWound(TroopId troopId, WoundType woundType, WoundSeverity severity, Instant now) {
        Troop troop = getTroopRepository().findTroop(troopId)
                .orElseThrow(() -> new HealingValidationException("Troop was not found."));
        if (troop.status() == TroopStatus.DEAD || troop.status() == TroopStatus.CAPTURED) {
            throw new HealingValidationException("Cannot assign wound to troop while " + troop.status().name().toLowerCase() + ".");
        }
        if (troop.status() != TroopStatus.WOUNDED) {
            getTroopRepository().saveTroop(troop.withStatus(TroopStatus.WOUNDED));
        }
        TroopWound wound = getTroopHealingRepository().saveWound(TroopWound.active(troopId, woundType, severity, now));
        getDomainEventPublisher().publish(new SimpleDomainEvent(
                "TroopWoundAssignedEvent",
                now,
                Map.of("troopId", troopId.value().toString(), "woundId", wound.woundId().toString(), "woundType", woundType.name())
        ));
        return wound;
    }
}
