package org.tavall.minecraft.server;

import java.util.List;

public final class KingdomCompanionTrainCommand extends KingdomForwardingCommand {
    @Override
    protected String rootToken() {
        return "companion train";
    }

    @Override
    protected String usage() {
        return "Usage: /kd companion train <companionId>";
    }

    @Override
    protected List<String> subcommands() {
        return List.of();
    }
}
