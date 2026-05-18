package org.tavall.control.resource;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.resources.ResourceType;

import java.util.UUID;

public interface IResourceService extends IDependencyInjectableInterface {
    PlayerGameState addResource(UUID playerId, ResourceType type, int amount);

    PlayerGameState setResource(UUID playerId, ResourceType type, int amount);
}
