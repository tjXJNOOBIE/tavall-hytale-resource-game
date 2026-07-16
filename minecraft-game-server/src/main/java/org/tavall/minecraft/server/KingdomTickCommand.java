package org.tavall.minecraft.server;

import java.util.List;

public final class KingdomTickCommand extends KingdomForwardingCommand {
    @Override
    protected String rootToken() {
        return "tick";
    }

    @Override
    protected String usage() {
        return "Usage: /kd tick [run|healing|clock]";
    }

    @Override
    protected List<String> subcommands() {
        return List.of("run", "healing", "clock");
    }
}
