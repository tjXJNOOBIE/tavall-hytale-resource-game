package com.tavall.resourcegame.frontend.minecraft.server;

import java.util.List;

public final class KingdomCompanionCreateCommand extends KingdomForwardingCommand {
    @Override
    protected String rootToken() {
        return "companion create";
    }

    @Override
    protected String usage() {
        return "Usage: /kd companion create [ownerPlayerId] <type>";
    }

    @Override
    protected List<String> subcommands() {
        return List.of("HEALER", "BRAWLER", "BRUTE", "ARCANE");
    }
}
