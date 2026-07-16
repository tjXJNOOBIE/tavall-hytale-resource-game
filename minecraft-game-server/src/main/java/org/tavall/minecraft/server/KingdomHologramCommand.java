package org.tavall.minecraft.server;

import java.util.List;

public final class KingdomHologramCommand extends KingdomForwardingCommand {
    @Override
    protected String rootToken() {
        return "hologram";
    }

    @Override
    protected String usage() {
        return "Usage: /kd hologram [spawn|stack|status|clear]";
    }

    @Override
    protected List<String> subcommands() {
        return List.of("spawn", "stack", "status", "clear");
    }
}
