package com.tavall.resourcegame.frontend.minecraft.server;

import java.util.List;

public final class KingdomCompanionWallCommand extends KingdomForwardingCommand {
    @Override
    protected String rootToken() {
        return "companion wall";
    }

    @Override
    protected String usage() {
        return "Usage: /kd companion wall <assign|remove|debug> ...";
    }

    @Override
    protected List<String> subcommands() {
        return List.of("assign", "remove", "debug");
    }
}
