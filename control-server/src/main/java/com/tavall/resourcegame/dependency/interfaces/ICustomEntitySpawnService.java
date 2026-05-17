package com.tavall.resourcegame.dependency.interfaces;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface ICustomEntitySpawnService extends IDependencyInjectableInterface {
    void spawn(Player player, String roleToken);

    void clear(Player player);

    void handleInteract(PlayerInteractEvent event);

    boolean openFromTarget(Player player, Ref<EntityStore> targetRef);
}
