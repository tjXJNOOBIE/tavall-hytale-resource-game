package org.tavall.minecraft.server;

import org.tavall.minecraft.framework.game.ui.UiScreenKey;

import java.util.List;
import java.util.Optional;

public final class KingdomResourcesCommand extends KingdomForwardingCommand {
    @Override
    protected String rootToken() {
        return "resources";
    }

    @Override
    protected String usage() {
        return "Usage: /kd resources [give|add|set]";
    }

    @Override
    protected List<String> subcommands() {
        return List.of("give", "add", "set");
    }

    @Override
    protected Optional<UiScreenKey> defaultPage() {
        return Optional.of(UiScreenKey.CASTLE_RESOURCES);
    }
}
