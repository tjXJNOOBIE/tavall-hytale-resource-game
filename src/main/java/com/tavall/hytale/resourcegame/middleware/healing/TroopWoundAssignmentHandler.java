package com.tavall.hytale.resourcegame.middleware.healing;

import com.tavall.hytale.resourcegame.middleware.event.DomainEventPublisher;
import com.tavall.hytale.resourcegame.middleware.event.SimpleDomainEvent;
import com.tavall.hytale.resourcegame.middleware.troop.Troop;
import com.tavall.hytale.resourcegame.middleware.troop.TroopId;
import com.tavall.hytale.resourcegame.middleware.troop.TroopRepository;
import com.tavall.hytale.resourcegame.middleware.troop.TroopStatus;

import java.time.Instant;
import java.util.Map;

public final class TroopWoundAssignmentHandler {
    private final TroopRepository troopRepository;
    private final TroopHealingRepository troopHealingRepository;
    private final DomainEventPublisher domainEventPublisher;

    public TroopWoundAssignmentHandler(
            TroopRepository troopRepository,
            TroopHealingRepository troopHealingRepository,
            DomainEventPublisher domainEventPublisher
    ) {
        this.troopRepository = troopRepository;
        this.troopHealingRepository = troopHealingRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    public TroopWound assignWound(TroopId troopId, WoundType woundType, WoundSeverity severity, Instant now) {
        Troop troop = troopRepository.findTroop(troopId)
                .orElseThrow(() -> new HealingValidationException("Troop was not found."));
        if (troop.status() == TroopStatus.DEAD || troop.status() == TroopStatus.CAPTURED) {
            throw new HealingValidationException("Cannot assign wound to troop while " + troop.status().name().toLowerCase() + ".");
        }
        if (troop.status() != TroopStatus.WOUNDED) {
            troopRepository.saveTroop(troop.withStatus(TroopStatus.WOUNDED));
        }
        TroopWound wound = troopHealingRepository.saveWound(TroopWound.active(troopId, woundType, severity, now));
        domainEventPublisher.publish(new SimpleDomainEvent(
                "TroopWoundAssignedEvent",
                now,
                Map.of("troopId", troopId.value().toString(), "woundId", wound.woundId().toString(), "woundType", woundType.name())
        ));
        return wound;
    }
}
