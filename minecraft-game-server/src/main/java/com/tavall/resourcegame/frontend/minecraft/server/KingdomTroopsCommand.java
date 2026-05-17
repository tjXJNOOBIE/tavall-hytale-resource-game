package com.tavall.resourcegame.frontend.minecraft.server;

import com.tavall.resourcegame.api.internal.ui.UiPageType;

import java.util.List;
import java.util.Optional;

public final class KingdomTroopsCommand extends KingdomForwardingCommand {
    @Override
    protected String rootToken() {
        return "troops";
    }

    @Override
    protected String usage() {
        return "Usage: /kd troops [debug|wound|heal|add|set]";
    }

    @Override
    protected List<String> subcommands() {
        return List.of("debug", "wound", "heal", "add", "set");
    }

    @Override
    protected Optional<UiPageType> defaultPage() {
        return Optional.of(UiPageType.CASTLE_TROOPS);
    }
}
