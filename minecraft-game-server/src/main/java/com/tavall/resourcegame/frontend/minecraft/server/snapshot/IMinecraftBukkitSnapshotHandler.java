package com.tavall.resourcegame.frontend.minecraft.server.snapshot;

import com.tavall.resourcegame.api.internal.minecraft.MinecraftServerRuntimeSnapshot;
import com.tavall.resourcegame.frontend.minecraft.server.view.MinecraftBukkitServerView;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IMinecraftBukkitSnapshotHandler extends IDependencyInjectableInterface {
    MinecraftServerRuntimeSnapshot createSnapshot(MinecraftBukkitServerView serverView, long observedAtEpochMillis);
}
