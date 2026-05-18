package org.tavall.control.building;

import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

/**
 * Handles interaction-driven placement confirmation.
 */
public interface IPlacementInteractionService extends IDependencyInjectableInterface {
    void handleInteract(PlayerInteractEvent event);
}

