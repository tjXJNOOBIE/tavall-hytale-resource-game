package org.tavall.control.castle;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import org.tavall.control.domain.PlayerGameState;

import java.util.Optional;
import java.util.UUID;

public interface ICastleBuildingVisualHandler extends IDependencyInjectableInterface {
    void ensureBuildings(UUID playerId, PlayerGameState state);

    void refreshBuildings(UUID playerId, PlayerGameState state);

    void clearBuildings(UUID playerId);

    Optional<UUID> findBuildingId(UUID playerId, Ref<EntityStore> targetRef);
}

