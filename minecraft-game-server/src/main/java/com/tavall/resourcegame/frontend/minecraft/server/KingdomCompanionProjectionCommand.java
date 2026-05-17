package com.tavall.resourcegame.frontend.minecraft.server;

import java.util.List;

public final class KingdomCompanionProjectionCommand extends KingdomForwardingCommand {
    @Override
    protected String rootToken() {
        return "companion projection";
    }

    @Override
    protected String usage() {
        return "Usage: /kd companion projection <companionId>";
    }

    @Override
    protected List<String> subcommands() {
        return List.of();
    }
}
