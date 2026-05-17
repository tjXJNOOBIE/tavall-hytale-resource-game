package com.tavall.resourcegame.frontend.minecraft.server;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.io.IOException;

public final class MinecraftBukkitJsonHandler implements IMinecraftBukkitJsonHandler, IMinecraftBukkitServerDomain, IDependencyInjectableConcrete {
    @Override
    public String writeJson(Object value) throws IOException {
        return getMinecraftBukkitJsonMapper().objectMapper().writeValueAsString(value);
    }
}
