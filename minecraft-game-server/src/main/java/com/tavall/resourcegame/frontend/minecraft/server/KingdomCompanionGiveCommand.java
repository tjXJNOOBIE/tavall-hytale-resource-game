package com.tavall.resourcegame.frontend.minecraft.server;

import java.util.List;

public final class KingdomCompanionGiveCommand extends KingdomForwardingCommand {
    @Override
    protected String rootToken() {
        return "companion give";
    }

    @Override
    protected String usage() {
        return "Usage: /kd companion give [ownerPlayerId] <type>";
    }

    @Override
    protected List<String> subcommands() {
        return List.of("HEALER", "BRAWLER", "BRUTE", "ARCANE");
    }
}
