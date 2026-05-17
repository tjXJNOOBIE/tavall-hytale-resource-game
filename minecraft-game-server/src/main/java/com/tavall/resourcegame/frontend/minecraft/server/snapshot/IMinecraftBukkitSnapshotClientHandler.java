package com.tavall.resourcegame.frontend.minecraft.server.snapshot;

import com.tavall.resourcegame.api.internal.minecraft.MinecraftServerRuntimeSnapshot;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.io.IOException;

public interface IMinecraftBukkitSnapshotClientHandler extends IDependencyInjectableInterface {
    boolean submitSnapshot(MinecraftServerRuntimeSnapshot snapshot) throws IOException;
}
