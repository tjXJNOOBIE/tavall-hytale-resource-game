package org.tavall.control.interior;

import com.hypixel.hytale.server.core.universe.world.World;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IInteriorInstanceHandler extends IDependencyInjectableInterface {
    CompletableFuture<World> resolveInteriorWorld(UUID playerId);

    CompletableFuture<World> warmInteriorWorld();

    String worldNameFor(UUID playerId);

    void releaseInteriorWorld(UUID playerId);

    void pruneTransientWorlds();
}

