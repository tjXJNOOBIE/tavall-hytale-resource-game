package com.tavall.resourcegame.frontend.minecraft.server;

import java.util.List;

public final class KingdomCompanionXpCommand extends KingdomForwardingCommand {
    @Override
    protected String rootToken() {
        return "companion xp";
    }

    @Override
    protected String usage() {
        return "Usage: /kd companion xp <companionId> <amount>";
    }

    @Override
    protected List<String> subcommands() {
        return List.of();
    }
}
