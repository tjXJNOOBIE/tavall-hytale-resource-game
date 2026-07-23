package org.tavall.minecraft.server;

import org.tavall.dependency.IDependencyInjectableConcrete;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class MinecraftBukkitInteractionSessionTracker implements IMinecraftBukkitInteractionSessionTracker, IDependencyInjectableConcrete {
    private final ConcurrentMap<UUID, MinecraftBukkitInteractionTarget> currentTargets = new ConcurrentHashMap<UUID, MinecraftBukkitInteractionTarget>();

    @Override
    public void remember(UUID playerId, MinecraftBukkitInteractionTarget target) {
        if (playerId == null || target == null) {
            return;
        }
        currentTargets.put(playerId, target);
    }

    @Override
    public Optional<MinecraftBukkitInteractionTarget> current(UUID playerId) {
        if (playerId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(currentTargets.get(playerId));
    }

    @Override
    public void clear(UUID playerId) {
        if (playerId != null) {
            currentTargets.remove(playerId);
        }
    }
}
