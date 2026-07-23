package org.tavall.minecraft.server.snapshot;

import org.tavall.api.minecraft.MinecraftServerRuntimeSnapshot;
import org.tavall.dependency.IDependencyInjectableInterface;

import java.io.IOException;

public interface IMinecraftBukkitSnapshotClientHandler extends IDependencyInjectableInterface {
    boolean submitSnapshot(MinecraftServerRuntimeSnapshot snapshot) throws IOException;
}
