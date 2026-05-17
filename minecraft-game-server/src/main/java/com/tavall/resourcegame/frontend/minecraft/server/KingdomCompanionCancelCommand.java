package com.tavall.resourcegame.frontend.minecraft.server;

import java.util.List;

public final class KingdomCompanionCancelCommand extends KingdomForwardingCommand {
    @Override
    protected String rootToken() {
        return "companion cancel";
    }

    @Override
    protected String usage() {
        return "Usage: /kd companion cancel <companionId>";
    }

    @Override
    protected List<String> subcommands() {
        return List.of();
    }
}
