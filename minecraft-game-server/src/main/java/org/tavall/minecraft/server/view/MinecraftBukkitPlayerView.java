package org.tavall.minecraft.server.view;

import java.util.UUID;

public interface MinecraftBukkitPlayerView {
    UUID playerId();

    String playerName();

    String worldName();

    double x();

    double y();

    double z();

    float yaw();

    float pitch();

    double health();

    int foodLevel();

    String gameMode();

    boolean online();
}
