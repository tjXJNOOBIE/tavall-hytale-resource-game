package com.tavall.resourcegame.frontend.minecraft.server;

import com.tavall.resourcegame.middleware.control.ControlCommandRuntime;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IMinecraftBukkitDirectControlRuntimeHandler extends IDependencyInjectableInterface {
    ControlCommandRuntime controlRuntime();
}
