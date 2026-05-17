package com.tavall.resourcegame.frontend.minecraft.server;

import java.util.List;

public final class KingdomCompanionSkillCommand extends KingdomForwardingCommand {
    @Override
    protected String rootToken() {
        return "companion skill";
    }

    @Override
    protected String usage() {
        return "Usage: /kd companion skill <unlock|upgrade> <companionId> <skillId>";
    }

    @Override
    protected List<String> subcommands() {
        return List.of("unlock", "upgrade");
    }
}
