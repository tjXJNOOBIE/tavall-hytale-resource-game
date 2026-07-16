package org.tavall.minecraft.server;

import java.util.List;

public final class KingdomCompanionBehaviorCommand extends KingdomForwardingCommand {
    @Override
    protected String rootToken() {
        return "companion behavior";
    }

    @Override
    protected String usage() {
        return "Usage: /kd companion behavior <companionId> <state>";
    }

    @Override
    protected List<String> subcommands() {
        return List.of("IDLE", "FOLLOW", "GUARD", "GATHER", "DEFEND");
    }
}
