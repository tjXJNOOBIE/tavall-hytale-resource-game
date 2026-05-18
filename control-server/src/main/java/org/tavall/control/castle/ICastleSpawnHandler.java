package org.tavall.control.castle;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import org.tavall.control.domain.CastleLocationData;

import java.util.UUID;

public interface ICastleSpawnHandler extends IDependencyInjectableInterface {
    void ensureCastleSpawned(Player player, CastleLocationData locationData);

    void replaceCastle(UUID playerId, CastleLocationData locationData);
}

