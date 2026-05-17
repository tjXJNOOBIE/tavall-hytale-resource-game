package com.tavall.resourcegame.frontend.minecraft.commands.source;

import com.velocitypowered.api.proxy.ConsoleCommandSource;

import java.util.Map;

public final class ConsoleVelocityCommandSource implements MinecraftVelocityCommandSource {
    private final ConsoleCommandSource console;

    public ConsoleVelocityCommandSource(ConsoleCommandSource console) {
        this.console = console;
    }

    @Override
    public String platformAccountId() {
        return "velocity-console";
    }

    @Override
    public String platformDisplayName() {
        return "Velocity Console";
    }

    @Override
    public String sourceType() {
        return "console";
    }

    @Override
    public Map<String, String> metadata() {
        return Map.of();
    }

    @Override
    public boolean hasPermission(String permission) {
        return console.hasPermission(permission);
    }
}
