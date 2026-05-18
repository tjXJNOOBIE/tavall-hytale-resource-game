package org.tavall.minecraft.server;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

public record MinecraftBukkitRuntimeState(long startedAtEpochMillis) implements IMinecraftBukkitRuntimeState, IDependencyInjectableConcrete {
}
