package org.tavall.minecraft.server.snapshot;

import org.tavall.api.minecraft.MinecraftServerRuntimeSnapshot;
import org.tavall.minecraft.server.view.MinecraftBukkitServerView;
import org.tavall.dependency.IDependencyInjectableInterface;

public interface IMinecraftBukkitSnapshotHandler extends IDependencyInjectableInterface {
    MinecraftServerRuntimeSnapshot createSnapshot(MinecraftBukkitServerView serverView, long observedAtEpochMillis);
}
