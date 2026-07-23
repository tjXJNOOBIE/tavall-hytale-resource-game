package org.tavall.control.resource;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.tavall.dependency.IDependencyInjectableInterface;
import org.tavall.control.domain.PlayerGameState;

import java.util.Optional;
import java.util.UUID;

public interface IResourceNodeVisualHandler extends IDependencyInjectableInterface {
    void ensureNodes(UUID playerId, PlayerGameState state);

    void refreshNodes(UUID playerId, PlayerGameState state);

    void clearNodes(UUID playerId);

    Optional<UUID> findNodeId(UUID playerId, Ref<EntityStore> targetRef);
}

