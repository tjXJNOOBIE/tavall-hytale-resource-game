package org.tavall.control.troop;

import org.tavall.control.common.CanonicalLocation;

public final class TroopMovementHandler implements ITroopDomain {
    public TroopMovementHandler() {
    }

    public TroopMovementHandler(TroopRepository troopRepository) {
        registerTroopRepository(troopRepository);
    }

    public Troop moveTroop(TroopId troopId, CanonicalLocation destination) {
        Troop troop = getTroopRepository().findTroop(troopId)
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
        return getTroopRepository().saveTroop(moved);
    }
}
