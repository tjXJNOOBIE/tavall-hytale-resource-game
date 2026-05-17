package com.tavall.resourcegame.middleware.control;

public final class HytaleCommandFanoutHandler {
    private final PlatformFrontendAdapter hytaleAdapter;

    public HytaleCommandFanoutHandler(PlatformFrontendAdapter hytaleAdapter) {
        this.hytaleAdapter = hytaleAdapter;
    }

    public PlatformCommandResult refreshHytaleProjection(ControlCommand command, java.util.List<String> changedObjectIds) {
        return hytaleAdapter.refreshProjection(command, changedObjectIds);
    }
}
