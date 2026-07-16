package org.tavall.minecraft.server;

import org.tavall.minecraft.framework.game.ui.UiScreenKey;

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
    protected Optional<UiScreenKey> defaultPage() {
        return Optional.of(UiScreenKey.CASTLE_MAIN);
    }
}
