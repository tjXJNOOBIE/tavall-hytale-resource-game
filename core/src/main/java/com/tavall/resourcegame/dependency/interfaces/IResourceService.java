package com.tavall.resourcegame.dependency.interfaces;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;
import com.tavall.resourcegame.domain.PlayerGameState;
import com.tavall.resourcegame.resources.ResourceType;

import java.util.UUID;

public interface IResourceService extends IDependencyInjectableInterface {
    PlayerGameState addResource(UUID playerId, ResourceType type, int amount);

    PlayerGameState setResource(UUID playerId, ResourceType type, int amount);
}