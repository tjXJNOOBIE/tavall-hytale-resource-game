package org.tavall.minecraft.server.tasks;

import org.tavall.dependency.IDependencyInjectableInterface;

public interface IMinecraftBukkitTaskScheduler extends IDependencyInjectableInterface {
    void runAsync(Runnable runnable);

    void runLater(Runnable runnable, long delayTicks);
}
