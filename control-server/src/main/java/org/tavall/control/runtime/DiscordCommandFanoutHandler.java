package org.tavall.control.runtime;

public final class DiscordCommandFanoutHandler {
    private final PlatformFrontendAdapter discordAdapter;

    public DiscordCommandFanoutHandler(PlatformFrontendAdapter discordAdapter) {
        this.discordAdapter = discordAdapter;
    }

    public PlatformCommandResult refreshDiscordProjection(ControlCommand command, java.util.List<String> changedObjectIds) {
        return discordAdapter.refreshProjection(command, changedObjectIds);
    }
}
