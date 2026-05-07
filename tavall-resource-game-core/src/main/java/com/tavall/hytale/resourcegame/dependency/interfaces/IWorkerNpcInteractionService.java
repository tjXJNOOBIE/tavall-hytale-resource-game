package com.tavall.hytale.resourcegame.dependency.interfaces;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tavall.hytale.resourcegame.dependency.IDependencyInjectableInterface;

public interface IWorkerNpcInteractionService extends IDependencyInjectableInterface {
    void handleInteract(PlayerInteractEvent event);

    boolean openFromTarget(Player player, Ref<EntityStore> targetRef);
}
