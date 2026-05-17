package com.tavall.resourcegame.frontend.minecraft.server;

import java.util.List;

public final class KingdomCompanionClaimCommand extends KingdomForwardingCommand {
    @Override
    protected String rootToken() {
        return "companion claim";
    }

    @Override
    protected String usage() {
        return "Usage: /kd companion claim <companionId>";
    }

    @Override
    protected List<String> subcommands() {
        return List.of();
    }
}
