package com.tavall.resourcegame.frontend.minecraft.server;

import java.util.List;

public final class KingdomCompanionMoraleCommand extends KingdomForwardingCommand {
    @Override
    protected String rootToken() {
        return "companion morale";
    }

    @Override
    protected String usage() {
        return "Usage: /kd companion morale <companionId> <state>";
    }

    @Override
    protected List<String> subcommands() {
        return List.of("CALM", "FOCUSED", "INSPIRED", "EXHAUSTED");
    }
}
