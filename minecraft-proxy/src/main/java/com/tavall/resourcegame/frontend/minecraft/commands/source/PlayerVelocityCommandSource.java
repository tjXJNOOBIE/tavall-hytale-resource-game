package com.tavall.resourcegame.frontend.minecraft.commands.source;

import com.velocitypowered.api.proxy.Player;

import java.util.Map;

public final class PlayerVelocityCommandSource implements MinecraftVelocityCommandSource {
    private final Player player;

    public PlayerVelocityCommandSource(Player player) {
        this.player = player;
    }

    @Override
    public String platformAccountId() {
        return player.getUniqueId().toString();
    }

    @Override
    public String platformDisplayName() {
        return player.getUsername();
    }

    @Override
    public String sourceType() {
        return "player";
    }

    @Override
    public Map<String, String> metadata() {
        return player.getCurrentServer()
                .map(serverConnection -> Map.of("server", serverConnection.getServerInfo().getName()))
                .orElseGet(Map::of);
    }

    @Override
    public boolean hasPermission(String permission) {
        return player.hasPermission(permission);
    }
}
