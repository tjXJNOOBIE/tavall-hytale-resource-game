package org.tavall.minecraft.server;

import org.tavall.minecraft.framework.game.ui.UiScreenKey;

import java.util.List;
import java.util.Optional;

public final class KingdomNodesCommand extends KingdomForwardingCommand {
    @Override
    protected String rootToken() {
        return "nodes";
    }

    @Override
    protected String usage() {
        return "Usage: /kd nodes [goto|align|status|place|list|select|assign|add|pillage|stock|recall|remove|clear]";
    }

    @Override
    protected List<String> subcommands() {
        return List.of("goto", "align", "status", "place", "list", "select", "assign", "add", "pillage", "stock", "recall", "remove", "clear");
    }

    @Override
    protected Optional<UiScreenKey> defaultPage() {
        return Optional.of(UiScreenKey.DEBUG_WORLD);
    }
}
