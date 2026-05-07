package com.tavall.hytale.resourcegame.frontend.minecraft.server;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class MinecraftBukkitPlayerSnapshot {
    private final UUID playerId;
    private final String playerName;
    private final String worldName;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;
    private final double health;
    private final int foodLevel;
    private final String gameMode;
    private final boolean online;
    private final Map<String, String> metadata;

    public MinecraftBukkitPlayerSnapshot(MinecraftBukkitPlayerView playerView) {
        this.playerId = playerView.playerId();
        this.playerName = playerView.playerName();
        this.worldName = playerView.worldName();
        this.x = playerView.x();
        this.y = playerView.y();
        this.z = playerView.z();
        this.yaw = playerView.yaw();
        this.pitch = playerView.pitch();
        this.health = playerView.health();
        this.foodLevel = playerView.foodLevel();
        this.gameMode = playerView.gameMode();
        this.online = playerView.online();
        this.metadata = new LinkedHashMap<String, String>();
        this.metadata.put("source", "bukkit-server-plugin");
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getWorldName() {
        return worldName;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public double getHealth() {
        return health;
    }

    public int getFoodLevel() {
        return foodLevel;
    }

    public String getGameMode() {
        return gameMode;
    }

    public boolean isOnline() {
        return online;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }
}
