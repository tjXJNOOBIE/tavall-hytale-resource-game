package org.tavall.minecraft.server.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.tavall.dependency.IDependencyInjectableConcrete;

public final class MinecraftBukkitJsonMapper implements IMinecraftBukkitJsonMapper, IDependencyInjectableConcrete {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ObjectMapper objectMapper() {
        return objectMapper;
    }
}
