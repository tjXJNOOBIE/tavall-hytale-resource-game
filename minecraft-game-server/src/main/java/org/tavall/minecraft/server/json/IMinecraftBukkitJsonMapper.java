package org.tavall.minecraft.server.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IMinecraftBukkitJsonMapper extends IDependencyInjectableInterface {
    ObjectMapper objectMapper();
}
