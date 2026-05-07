package com.tavall.hytale.resourcegame.frontend.minecraft.server;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class BukkitPlayerViewAdapter implements MinecraftBukkitPlayerView {
    private final Player player;

    public BukkitPlayerViewAdapter(Player player) {
        this.player = player;
    }

    @Override
    public UUID playerId() {
        return player.getUniqueId();
    }

    @Override
    public String playerName() {
        return player.getName();
    }

    @Override
    public String worldName() {
        return player.getWorld().getName();
    }

    @Override
    public double x() {
        return location().getX();
    }

    @Override
    public double y() {
        return location().getY();
    }

    @Override
    public double z() {
        return location().getZ();
    }

    @Override
    public float yaw() {
        return location().getYaw();
    }

    @Override
    public float pitch() {
        return location().getPitch();
    }

    @Override
    public double health() {
        return player.getHealth();
    }

    @Override
    public int foodLevel() {
        return player.getFoodLevel();
    }

    @Override
    public String gameMode() {
        return player.getGameMode().name();
    }

    @Override
    public boolean online() {
        return player.isOnline();
    }

    private Location location() {
        return player.getLocation();
    }
}
