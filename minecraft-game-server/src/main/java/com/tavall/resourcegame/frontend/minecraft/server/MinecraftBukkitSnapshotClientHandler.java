package com.tavall.resourcegame.frontend.minecraft.server;

import com.tavall.resourcegame.dependency.DependencyLoaderAccess;
import com.tavall.resourcegame.middleware.cloud.IMinecraftServerSnapshotIngressHandler;
import com.tavall.resourcegame.middleware.cloud.MinecraftServerSnapshotIngressHandler;
import com.tavall.resourcegame.shared.frontend.MinecraftServerRuntimeSnapshot;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.io.IOException;
import java.time.Instant;

public final class MinecraftBukkitSnapshotClientHandler implements IMinecraftBukkitSnapshotClientHandler, IMinecraftBukkitServerDomain, IDependencyInjectableConcrete {
    @Override
    public boolean submitSnapshot(MinecraftServerRuntimeSnapshot snapshot) throws IOException {
        return snapshotIngressHandler().ingest(snapshot, Instant.now()).accepted();
    }

    private IMinecraftServerSnapshotIngressHandler snapshotIngressHandler() {
        return DependencyLoaderAccess.findOptionalInstance(IMinecraftServerSnapshotIngressHandler.class)
                .orElseGet(this::createSnapshotIngressHandler);
    }

    private IMinecraftServerSnapshotIngressHandler createSnapshotIngressHandler() {
        IMinecraftServerSnapshotIngressHandler handler = new MinecraftServerSnapshotIngressHandler();
        DependencyLoaderAccess.registerInstance(IMinecraftServerSnapshotIngressHandler.class, handler);
        return handler;
    }
}
