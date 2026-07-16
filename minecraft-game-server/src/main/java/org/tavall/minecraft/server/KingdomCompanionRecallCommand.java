package org.tavall.minecraft.server;

import java.util.List;

public final class KingdomCompanionRecallCommand extends KingdomForwardingCommand {
    @Override
    protected String rootToken() {
        return "companion recall";
    }

    @Override
    protected String usage() {
        return "Usage: /kd companion recall <companionId>";
    }

    @Override
    protected List<String> subcommands() {
        return List.of();
    }
}
