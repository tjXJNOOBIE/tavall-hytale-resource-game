package org.tavall.minecraft;

import org.tavall.minecraft.commands.source.MinecraftVelocityCommandSource;

import java.util.Map;
import java.util.Set;

record TestVelocityCommandSource(Set<String> permissions) implements MinecraftVelocityCommandSource {
    @Override
    public String platformAccountId() {
        return "test-player";
    }

    @Override
    public String platformDisplayName() {
        return "Test Player";
    }

    @Override
    public String sourceType() {
        return "test";
    }

    @Override
    public Map<String, String> metadata() {
        return Map.of("server", "lobby");
    }

    @Override
    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }
}
