package org.tavall.minecraft.server;

import java.util.List;

public final class KingdomCompanionSetLevelCommand extends KingdomForwardingCommand {
    @Override
    protected String rootToken() {
        return "companion setlevel";
    }

    @Override
    protected String usage() {
        return "Usage: /kd companion setlevel <companionId> <level>";
    }

    @Override
    protected List<String> subcommands() {
        return List.of();
    }
}
