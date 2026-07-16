package org.tavall.minecraft.server;

import java.util.List;

public final class KingdomCompanionDebugCommand extends KingdomForwardingCommand {
    @Override
    protected String rootToken() {
        return "companion debug";
    }

    @Override
    protected String usage() {
        return "Usage: /kd companion debug <companionId>";
    }

    @Override
    protected List<String> subcommands() {
        return List.of();
    }
}
