package org.tavall.control.troop;

import java.util.Optional;

public interface TroopRepository {
    Troop saveTroop(Troop troop);

    Optional<Troop> findTroop(TroopId troopId);
}
