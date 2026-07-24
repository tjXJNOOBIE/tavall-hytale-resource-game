package org.tavall.minecraft.server.logging;

import org.tavall.dependency.IDependencyInjectableInterface;

public interface IMinecraftBukkitLogger extends IDependencyInjectableInterface {
    void info(String message);

    void warning(String message);
}
