package org.tavall.control.building;

import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import org.tavall.dependency.IDependencyInjectableInterface;

/**
 * Handles interaction-driven placement confirmation.
 */
public interface IPlacementInteractionHandler extends IDependencyInjectableInterface {
    void handleInteract(PlayerInteractEvent event);
}

