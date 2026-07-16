package org.tavall.minecraft.server.tasks;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IMinecraftBukkitTaskScheduler extends IDependencyInjectableInterface {
    void runAsync(Runnable runnable);

    void runLater(Runnable runnable, long delayTicks);
}
