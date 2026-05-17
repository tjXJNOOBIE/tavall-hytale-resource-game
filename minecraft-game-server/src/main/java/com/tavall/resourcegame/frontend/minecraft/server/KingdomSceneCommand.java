package com.tavall.resourcegame.frontend.minecraft.server;

import com.tavall.resourcegame.api.internal.ui.UiPageType;

import java.util.List;
import java.util.Optional;

public final class KingdomSceneCommand extends KingdomForwardingCommand {
    @Override
    protected String rootToken() {
        return "scene";
    }

    @Override
    protected String usage() {
        return "Usage: /kd scene [refresh]";
    }

    @Override
    protected List<String> subcommands() {
        return List.of("refresh");
    }

    @Override
    protected Optional<UiPageType> defaultPage() {
        return Optional.of(UiPageType.DEBUG_WORLD);
    }
}
