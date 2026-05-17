package com.tavall.resourcegame.frontend.minecraft.server;

import com.tavall.resourcegame.api.internal.ui.UiPageType;

import java.util.List;
import java.util.Optional;

public final class KingdomPlaceCommand extends KingdomForwardingCommand {
    @Override
    protected String rootToken() {
        return "place";
    }

    @Override
    protected String usage() {
        return "Usage: /kd place [castle|node|building|confirm|cancel|status|preview|move]";
    }

    @Override
    protected List<String> subcommands() {
        return List.of("castle", "node", "building", "confirm", "cancel", "status", "preview", "move");
    }

    @Override
    protected Optional<UiPageType> defaultPage() {
        return Optional.of(UiPageType.DEBUG_PLACEMENT);
    }
}
