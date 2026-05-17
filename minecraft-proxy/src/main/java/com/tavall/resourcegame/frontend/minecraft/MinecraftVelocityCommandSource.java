package com.tavall.resourcegame.frontend.minecraft;

import java.util.Map;

public interface MinecraftVelocityCommandSource {
    String platformAccountId();

    String platformDisplayName();

    String sourceType();

    Map<String, String> metadata();

    boolean hasPermission(String permission);
}
