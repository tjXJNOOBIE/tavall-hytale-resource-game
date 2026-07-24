package org.tavall.minecraft.server.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.tavall.dependency.IDependencyInjectableInterface;

public interface IMinecraftBukkitJsonMapper extends IDependencyInjectableInterface {
    ObjectMapper objectMapper();
}
