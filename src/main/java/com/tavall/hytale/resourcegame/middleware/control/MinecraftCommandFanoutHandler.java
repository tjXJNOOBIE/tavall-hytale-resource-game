package com.tavall.hytale.resourcegame.middleware.control;

public final class MinecraftCommandFanoutHandler {
    private final PlatformFrontendAdapter minecraftAdapter;

    public MinecraftCommandFanoutHandler(PlatformFrontendAdapter minecraftAdapter) {
        this.minecraftAdapter = minecraftAdapter;
    }

    public PlatformCommandResult refreshMinecraftProjection(ControlCommand command, java.util.List<String> changedObjectIds) {
        return minecraftAdapter.refreshProjection(command, changedObjectIds);
    }
}
