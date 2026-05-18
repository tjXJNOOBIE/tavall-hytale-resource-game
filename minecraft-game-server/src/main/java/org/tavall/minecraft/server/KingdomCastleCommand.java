package org.tavall.minecraft.server;

import org.tavall.api.minecraft.ui.UiPageType;

import java.util.List;
import java.util.Optional;

public final class KingdomCastleCommand extends KingdomForwardingCommand {
    @Override
    protected String rootToken() {
        return "castle";
    }

    @Override
    protected String usage() {
        return "Usage: /kd castle [align|move|open|goto|refresh]";
    }

    @Override
    protected List<String> subcommands() {
        return List.of("align", "move", "open", "goto", "refresh");
    }

    @Override
    protected Optional<UiPageType> defaultPage() {
        return Optional.of(UiPageType.CASTLE_MAIN);
    }
}
