package com.tavall.resourcegame.frontend.minecraft.server;

import java.util.List;

public final class KingdomBootstrapCommand extends KingdomForwardingCommand {
    @Override
    protected String rootToken() {
        return "bootstrap";
    }

    @Override
    protected String usage() {
        return "Usage: /kd bootstrap";
    }

    @Override
    protected List<String> subcommands() {
        return List.of();
    }
}
