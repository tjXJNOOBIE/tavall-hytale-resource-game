package org.tavall.minecraft.server.json;

import org.tavall.dependency.IDependencyInjectableConcrete;

import java.io.IOException;

public final class MinecraftBukkitJsonHandler implements IMinecraftBukkitJsonHandler, org.tavall.minecraft.server.MinecraftBukkitServerDomain, IDependencyInjectableConcrete {
    @Override
    public String writeJson(Object value) throws IOException {
        return getMinecraftBukkitJsonMapper().objectMapper().writeValueAsString(value);
    }
}
