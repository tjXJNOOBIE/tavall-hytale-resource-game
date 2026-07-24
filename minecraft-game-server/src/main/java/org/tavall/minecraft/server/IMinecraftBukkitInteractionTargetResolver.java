package org.tavall.minecraft.server;

import org.tavall.dependency.IDependencyInjectableInterface;
import org.bukkit.entity.Entity;

import java.util.Optional;

public interface IMinecraftBukkitInteractionTargetResolver extends IDependencyInjectableInterface {
    Optional<MinecraftBukkitInteractionTarget> resolve(Entity entity);
}
