package com.tavall.resourcegame.frontend.minecraft.server;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IMinecraftBukkitLogger extends IDependencyInjectableInterface {
    void info(String message);

    void warning(String message);
}
