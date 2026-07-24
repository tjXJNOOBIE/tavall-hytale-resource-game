package org.tavall.minecraft.server;

import org.tavall.dependency.IDependencyInjectableInterface;

public interface IMinecraftBukkitRuntimeState extends IDependencyInjectableInterface {
    long startedAtEpochMillis();
}
