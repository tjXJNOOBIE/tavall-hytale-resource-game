package com.tavall.resourcegame.frontend.minecraft.server;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IMinecraftBukkitRuntimeState extends IDependencyInjectableInterface {
    long startedAtEpochMillis();
}
