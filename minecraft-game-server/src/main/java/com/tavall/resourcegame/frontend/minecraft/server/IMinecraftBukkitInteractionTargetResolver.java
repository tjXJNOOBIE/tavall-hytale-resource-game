package com.tavall.resourcegame.frontend.minecraft.server;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import org.bukkit.entity.Entity;

import java.util.Optional;

public interface IMinecraftBukkitInteractionTargetResolver extends IDependencyInjectableInterface {
    Optional<MinecraftBukkitInteractionTarget> resolve(Entity entity);
}
