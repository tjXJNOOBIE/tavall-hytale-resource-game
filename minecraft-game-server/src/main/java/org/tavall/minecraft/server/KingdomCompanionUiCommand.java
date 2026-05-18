package org.tavall.minecraft.server;

import java.util.List;

public final class KingdomCompanionUiCommand extends KingdomForwardingCommand {
    @Override
    protected String rootToken() {
        return "companion ui";
    }

    @Override
    protected String usage() {
        return "Usage: /kd companion ui <summary|detail|skills|wall|projection>";
    }

    @Override
    protected List<String> subcommands() {
        return List.of("summary", "detail", "skills", "wall", "projection");
    }
}
