package com.tavall.resourcegame.frontend.minecraft.server.tasks;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IMinecraftBukkitTaskScheduler extends IDependencyInjectableInterface {
    void runAsync(Runnable runnable);
}
