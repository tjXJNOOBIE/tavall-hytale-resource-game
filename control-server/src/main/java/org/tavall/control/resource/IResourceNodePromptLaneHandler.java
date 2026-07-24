package org.tavall.control.resource;

import com.hypixel.hytale.server.core.entity.entities.Player;
import org.tavall.dependency.IDependencyInjectableInterface;
import org.tavall.control.domain.ResourceNodeData;

public interface IResourceNodePromptLaneHandler extends IDependencyInjectableInterface {
    void alignPlayer(Player player, ResourceNodeData node);
}

