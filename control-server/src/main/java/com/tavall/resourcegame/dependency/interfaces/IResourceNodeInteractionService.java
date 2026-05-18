package org.tavall.control.dependency.interfaces;

import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IResourceNodeInteractionService extends IDependencyInjectableInterface {
    void handleInteract(PlayerInteractEvent event);
}
