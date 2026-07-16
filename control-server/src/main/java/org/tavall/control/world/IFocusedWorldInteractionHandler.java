package org.tavall.control.world;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import org.tavall.control.domain.FocusedWorldTarget;

import java.util.Optional;
import java.util.UUID;

public interface IFocusedWorldInteractionHandler extends IDependencyInjectableInterface {
    Optional<FocusedWorldTarget> resolve(Player player);

    Optional<UUID> focusedNodeId(Player player);

    Optional<UUID> focusedBuildingId(Player player);

    Optional<FocusedWorldTarget> interact(Player player);
}

