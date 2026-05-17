package com.tavall.resourcegame.frontend.minecraft.server;

import java.util.UUID;

final class FakeMinecraftBukkitPlayerView implements MinecraftBukkitPlayerView {
    @Override
    public UUID playerId() {
        return UUID.fromString("00000000-0000-0000-0000-000000000001");
    }

    @Override
    public String playerName() {
        return "Miner";
    }

    @Override
    public String worldName() {
        return "world";
    }

    @Override
    public double x() {
        return 1.0d;
    }

    @Override
    public double y() {
        return 64.0d;
    }

    @Override
    public double z() {
        return 2.0d;
    }

    @Override
    public float yaw() {
        return 90.0f;
    }

    @Override
    public float pitch() {
        return 20.0f;
    }

    @Override
    public double health() {
        return 18.0d;
    }

    @Override
    public int foodLevel() {
        return 20;
    }

    @Override
    public String gameMode() {
        return "SURVIVAL";
    }

    @Override
    public boolean online() {
        return true;
    }
}
