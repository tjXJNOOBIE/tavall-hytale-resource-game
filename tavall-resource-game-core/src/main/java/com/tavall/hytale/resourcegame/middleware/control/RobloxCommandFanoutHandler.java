package com.tavall.hytale.resourcegame.middleware.control;

public final class RobloxCommandFanoutHandler {
    private final PlatformFrontendAdapter robloxAdapter;

    public RobloxCommandFanoutHandler(PlatformFrontendAdapter robloxAdapter) {
        this.robloxAdapter = robloxAdapter;
    }

    public PlatformCommandResult refreshRobloxProjection(ControlCommand command, java.util.List<String> changedObjectIds) {
        return robloxAdapter.refreshProjection(command, changedObjectIds);
    }
}
