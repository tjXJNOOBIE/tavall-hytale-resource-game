package org.tavall.minecraft.server;

import org.tavall.minecraft.framework.game.ui.UiScreenKey;

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
    protected Optional<UiScreenKey> defaultPage() {
        return Optional.of(UiScreenKey.CASTLE_TROOPS);
    }
}
