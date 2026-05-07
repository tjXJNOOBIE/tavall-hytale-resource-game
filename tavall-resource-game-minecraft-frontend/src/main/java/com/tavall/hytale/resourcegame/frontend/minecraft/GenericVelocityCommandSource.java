package com.tavall.hytale.resourcegame.frontend.minecraft;

import com.velocitypowered.api.command.CommandSource;

import java.util.Map;

public final class GenericVelocityCommandSource implements MinecraftVelocityCommandSource {
    private final CommandSource source;

    public GenericVelocityCommandSource(CommandSource source) {
        this.source = source;
    }

    @Override
    public String platformAccountId() {
        return "velocity-source";
    }

    @Override
    public String platformDisplayName() {
        return "Velocity Source";
    }

    @Override
    public String sourceType() {
        return "generic";
    }

    @Override
    public Map<String, String> metadata() {
        return Map.of();
    }

    @Override
    public boolean hasPermission(String permission) {
        return source.hasPermission(permission);
    }
}
