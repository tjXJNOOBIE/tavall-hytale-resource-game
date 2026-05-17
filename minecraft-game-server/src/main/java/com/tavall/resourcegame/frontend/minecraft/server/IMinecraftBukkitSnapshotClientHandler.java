package com.tavall.resourcegame.frontend.minecraft.server;

import com.tavall.resourcegame.shared.frontend.MinecraftServerRuntimeSnapshot;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.io.IOException;

public interface IMinecraftBukkitSnapshotClientHandler extends IDependencyInjectableInterface {
    boolean submitSnapshot(MinecraftServerRuntimeSnapshot snapshot) throws IOException;
}
