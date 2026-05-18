package org.tavall.control.resource;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import org.tavall.control.domain.ResourceNodeData;

public interface IResourceNodePromptLaneService extends IDependencyInjectableInterface {
    void alignPlayer(Player player, ResourceNodeData node);
}

