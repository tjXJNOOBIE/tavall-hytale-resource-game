package com.tavall.resourcegame.frontend.minecraft.server.snapshot;

import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import com.tavall.resourcegame.api.internal.minecraft.MinecraftServerRuntimeSnapshot;
import com.tavall.resourcegame.frontend.minecraft.server.IMinecraftBukkitServerDomain;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public final class MinecraftBukkitSnapshotClientHandler implements IMinecraftBukkitSnapshotClientHandler, IMinecraftBukkitServerDomain, IDependencyInjectableConcrete {
    private static final AtomicReference<MinecraftServerRuntimeSnapshot> LATEST_LOCAL_SNAPSHOT = new AtomicReference<>();

    @Override
    public boolean submitSnapshot(MinecraftServerRuntimeSnapshot snapshot) throws IOException {
        LATEST_LOCAL_SNAPSHOT.set(snapshot);
        DependencyLoaderAccess.registerInstance(MinecraftServerRuntimeSnapshot.class, snapshot);
        return true;
    }
}
