package org.tavall.minecraft.server;

import org.tavall.minecraft.framework.game.ui.UiScreenKey;

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
    protected Optional<UiScreenKey> defaultPage() {
        return Optional.of(UiScreenKey.DEBUG_PLACEMENT);
    }
}
