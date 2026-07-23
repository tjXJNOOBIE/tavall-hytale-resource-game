package org.tavall.minecraft.server;

import org.tavall.dependency.IDependencyInjectableInterface;

import java.util.Optional;
import java.util.UUID;

public interface IMinecraftBukkitInteractionSessionTracker extends IDependencyInjectableInterface {
    void remember(UUID playerId, MinecraftBukkitInteractionTarget target);

    Optional<MinecraftBukkitInteractionTarget> current(UUID playerId);

    void clear(UUID playerId);
}
