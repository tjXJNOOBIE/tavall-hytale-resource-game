package com.tavall.resourcegame.frontend.minecraft.server.json;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.io.IOException;

public interface IMinecraftBukkitJsonHandler extends IDependencyInjectableInterface {
    String writeJson(Object value) throws IOException;
}
