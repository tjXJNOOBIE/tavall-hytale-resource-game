package org.tavall.minecraft.server;

import org.tavall.dependency.IDependencyInjectableConcrete;

public record MinecraftBukkitRuntimeState(long startedAtEpochMillis) implements IMinecraftBukkitRuntimeState, IDependencyInjectableConcrete {
}
