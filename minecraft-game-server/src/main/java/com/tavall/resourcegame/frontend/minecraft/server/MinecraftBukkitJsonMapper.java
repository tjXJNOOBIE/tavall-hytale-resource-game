package com.tavall.resourcegame.frontend.minecraft.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

public final class MinecraftBukkitJsonMapper implements IMinecraftBukkitJsonMapper, IDependencyInjectableConcrete {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ObjectMapper objectMapper() {
        return objectMapper;
    }
}
