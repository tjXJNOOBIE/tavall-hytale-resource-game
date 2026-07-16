package org.tavall.minecraft.server;

import java.util.List;

public final class KingdomCompanionSummonCommand extends KingdomForwardingCommand {
    @Override
    protected String rootToken() {
        return "companion summon";
    }

    @Override
    protected String usage() {
        return "Usage: /kd companion summon <companionId>";
    }

    @Override
    protected List<String> subcommands() {
        return List.of();
    }
}
