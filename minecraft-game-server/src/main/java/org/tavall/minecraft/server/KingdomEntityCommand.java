package org.tavall.minecraft.server;

import java.util.List;

public final class KingdomEntityCommand extends KingdomForwardingCommand {
    @Override
    protected String rootToken() {
        return "entity";
    }

    @Override
    protected String usage() {
        return "Usage: /kd entity [spawn|clear|list]";
    }

    @Override
    protected List<String> subcommands() {
        return List.of("spawn", "clear", "list");
    }
}
