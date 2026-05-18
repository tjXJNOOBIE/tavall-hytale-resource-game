package org.tavall.control.support;

import com.hypixel.hytale.server.core.entity.entities.Player;
import org.tavall.control.dependency.interfaces.ICastleSpawnService;
import org.tavall.control.domain.CastleLocationData;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RecordingCastleSpawnService implements ICastleSpawnService {
    private final Map<UUID, CastleLocationData> replacedLocations = new ConcurrentHashMap<>();

    @Override
    public void ensureCastleSpawned(Player player, CastleLocationData locationData) {
    }

    @Override
    public void replaceCastle(UUID playerId, CastleLocationData locationData) {
        if (playerId == null || locationData == null) {
            return;
        }
        replacedLocations.put(playerId, locationData);
    }

    public CastleLocationData replacedLocation(UUID playerId) {
        return replacedLocations.get(playerId);
    }
}
