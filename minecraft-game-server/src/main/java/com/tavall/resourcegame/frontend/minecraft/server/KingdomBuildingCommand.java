package com.tavall.resourcegame.frontend.minecraft.server;

import com.tavall.resourcegame.ui.UiPageType;

import java.util.List;
import java.util.Optional;

public final class KingdomBuildingCommand extends KingdomForwardingCommand {
    @Override
    protected String rootToken() {
        return "building";
    }

    @Override
    protected String usage() {
        return "Usage: /kd building [open|overview|upgrade|storage|production|close]";
    }

    @Override
    protected List<String> subcommands() {
        return List.of("open", "overview", "upgrade", "storage", "production", "close");
    }

    @Override
    protected Optional<UiPageType> defaultPage() {
        return Optional.of(UiPageType.BUILDING_DETAIL);
    }
}
