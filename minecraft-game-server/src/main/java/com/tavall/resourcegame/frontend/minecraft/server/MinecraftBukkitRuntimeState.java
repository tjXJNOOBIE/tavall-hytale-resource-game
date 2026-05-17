package com.tavall.resourcegame.frontend.minecraft.server;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

public record MinecraftBukkitRuntimeState(long startedAtEpochMillis) implements IMinecraftBukkitRuntimeState, IDependencyInjectableConcrete {
}
