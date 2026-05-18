package org.tavall.minecraft.server.snapshot;

import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import org.tavall.api.minecraft.MinecraftServerRuntimeSnapshot;
import org.tavall.minecraft.server.IMinecraftBukkitServerDomain;
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
