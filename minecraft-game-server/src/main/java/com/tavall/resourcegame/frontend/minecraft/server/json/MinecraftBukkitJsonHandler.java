package org.tavall.minecraft.server.json;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.io.IOException;

public final class MinecraftBukkitJsonHandler implements IMinecraftBukkitJsonHandler, org.tavall.minecraft.server.IMinecraftBukkitServerDomain, IDependencyInjectableConcrete {
    @Override
    public String writeJson(Object value) throws IOException {
        return getMinecraftBukkitJsonMapper().objectMapper().writeValueAsString(value);
    }
}
