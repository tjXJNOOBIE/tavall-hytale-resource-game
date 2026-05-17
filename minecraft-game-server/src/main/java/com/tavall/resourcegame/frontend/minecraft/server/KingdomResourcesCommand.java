package com.tavall.resourcegame.frontend.minecraft.server;

import com.tavall.resourcegame.ui.UiPageType;

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
    protected Optional<UiPageType> defaultPage() {
        return Optional.of(UiPageType.CASTLE_RESOURCES);
    }
}
