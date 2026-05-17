package com.tavall.resourcegame.dependency.interfaces;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import com.tavall.resourcegame.domain.ResourceNodeData;

public interface IResourceNodePromptLaneService extends IDependencyInjectableInterface {
    void alignPlayer(Player player, ResourceNodeData node);
}
