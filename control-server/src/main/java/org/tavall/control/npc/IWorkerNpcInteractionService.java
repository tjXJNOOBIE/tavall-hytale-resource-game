package org.tavall.control.npc;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IWorkerNpcInteractionService extends IDependencyInjectableInterface {
    void handleInteract(PlayerInteractEvent event);

    boolean openFromTarget(Player player, Ref<EntityStore> targetRef);
}

