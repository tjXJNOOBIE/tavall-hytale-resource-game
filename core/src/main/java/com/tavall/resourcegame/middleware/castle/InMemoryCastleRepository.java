package com.tavall.resourcegame.middleware.castle;

import com.tavall.resourcegame.middleware.identity.UniversalPlayerId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryCastleRepository implements CastleRepository {
    private final Map<CastleId, Castle> castlesById = new ConcurrentHashMap<>();

    @Override
    public Castle saveCastle(Castle castle) {
        castlesById.put(castle.castleId(), castle);
        return castle;
    }

    @Override
    public Optional<Castle> findCastle(CastleId castleId) {
        return Optional.ofNullable(castlesById.get(castleId));
    }

    @Override
    public List<Castle> findCastlesForPlayer(UniversalPlayerId universalPlayerId) {
        List<Castle> castles = new ArrayList<>();
        for (Castle castle : castlesById.values()) {
            if (castle.ownerPlayerId().equals(universalPlayerId)) {
                castles.add(castle);
            }
        }
        return List.copyOf(castles);
    }
}
