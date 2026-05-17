package com.tavall.resourcegame.frontend.minecraft.server;

import com.tavall.resourcegame.api.internal.minecraft.MinecraftServerRuntimeSnapshot;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IMinecraftBukkitSnapshotHandler extends IDependencyInjectableInterface {
    MinecraftServerRuntimeSnapshot createSnapshot(MinecraftBukkitServerView serverView, long observedAtEpochMillis);
}
