package com.tavall.resourcegame.frontend.minecraft.server.snapshot;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IMinecraftBukkitSnapshotSubmitHandler extends IDependencyInjectableInterface {
    void submitSnapshotQuietly();
}
