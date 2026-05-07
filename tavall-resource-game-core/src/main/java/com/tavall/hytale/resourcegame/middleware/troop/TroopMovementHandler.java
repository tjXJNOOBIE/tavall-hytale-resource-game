package com.tavall.hytale.resourcegame.middleware.troop;

import com.tavall.hytale.resourcegame.middleware.common.CanonicalLocation;

public final class TroopMovementHandler {
    private final TroopRepository troopRepository;

    public TroopMovementHandler(TroopRepository troopRepository) {
        this.troopRepository = troopRepository;
    }

    public Troop moveTroop(TroopId troopId, CanonicalLocation destination) {
        Troop troop = troopRepository.findTroop(troopId)
                .orElseThrow(() -> new TroopValidationException("Troop was not found."));
        Troop moved = new Troop(
                troop.troopId(),
                troop.ownerPlayerId(),
                troop.ownerGuildId(),
                troop.troopType(),
                troop.tier(),
                troop.health(),
                TroopStatus.MARCHING,
                destination,
                troop.assignedCastleId(),
                troop.globalAssetId(),
                troop.metadata()
        );
        return troopRepository.saveTroop(moved);
    }
}
