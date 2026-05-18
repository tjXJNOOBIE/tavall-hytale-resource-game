package com.tavall.resourcegame.frontend.minecraft.server.logging;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.util.logging.Logger;

public final class MinecraftBukkitLoggerHandler implements IMinecraftBukkitLogger, IDependencyInjectableConcrete {
    private static final Logger LOGGER = Logger.getLogger("TavallResourceGameBukkit");

    @Override
    public void info(String message) {
        LOGGER.info(message);
    }

    @Override
    public void warning(String message) {
        LOGGER.warning(message);
    }
}
