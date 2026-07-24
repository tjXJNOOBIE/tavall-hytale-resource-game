package org.tavall.control.runtime;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.tavall.dependency.IDependencyInjectableInterface;

public interface ICustomEntitySpawnHandler extends IDependencyInjectableInterface {
    void spawn(Player player, String roleToken);

    void clear(Player player);

    void handleInteract(PlayerInteractEvent event);

    boolean openFromTarget(Player player, Ref<EntityStore> targetRef);
}

