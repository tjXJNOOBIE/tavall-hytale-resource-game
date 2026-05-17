package com.tavall.resourcegame.frontend.minecraft.server;

import com.tavall.resourcegame.ui.UiPageType;

import java.util.List;
import java.util.Optional;

public final class KingdomBuildingsCommand extends KingdomForwardingCommand {
    @Override
    protected String rootToken() {
        return "buildings";
    }

    @Override
    protected String usage() {
        return "Usage: /kd buildings [place|stage|spawn|list|status|select|align|goto|upgrade|cancel|finish|clear]";
    }

    @Override
    protected List<String> subcommands() {
        return List.of("place", "stage", "spawn", "list", "status", "select", "align", "goto", "upgrade", "cancel", "finish", "clear");
    }

    @Override
    protected Optional<UiPageType> defaultPage() {
        return Optional.of(UiPageType.CASTLE_BUILDINGS);
    }
}
