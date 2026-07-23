package org.tavall.control.resource;

import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import org.tavall.dependency.IDependencyInjectableInterface;

public interface IResourceNodeInteractionHandler extends IDependencyInjectableInterface {
    void handleInteract(PlayerInteractEvent event);
}

