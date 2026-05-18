package org.tavall.minecraft.server;

import java.util.List;

public final class KingdomCompanionListCommand extends KingdomForwardingCommand {
    @Override
    protected String rootToken() {
        return "companion list";
    }

    @Override
    protected String usage() {
        return "Usage: /kd companion list [ownerPlayerId]";
    }

    @Override
    protected List<String> subcommands() {
        return List.of();
    }
}
