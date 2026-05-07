package com.tavall.hytale.resourcegame.middleware.troop;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryTroopRepository implements TroopRepository {
    private final Map<TroopId, Troop> troopsById = new ConcurrentHashMap<>();

    @Override
    public Troop saveTroop(Troop troop) {
        troopsById.put(troop.troopId(), troop);
        return troop;
    }

    @Override
    public Optional<Troop> findTroop(TroopId troopId) {
        return Optional.ofNullable(troopsById.get(troopId));
    }
}
