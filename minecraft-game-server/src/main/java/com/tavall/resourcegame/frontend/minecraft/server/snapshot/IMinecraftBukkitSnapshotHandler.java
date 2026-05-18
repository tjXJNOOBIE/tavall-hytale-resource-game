package org.tavall.minecraft.server.snapshot;

import org.tavall.api.minecraft.MinecraftServerRuntimeSnapshot;
import org.tavall.minecraft.server.view.MinecraftBukkitServerView;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IMinecraftBukkitSnapshotHandler extends IDependencyInjectableInterface {
    MinecraftServerRuntimeSnapshot createSnapshot(MinecraftBukkitServerView serverView, long observedAtEpochMillis);
}
