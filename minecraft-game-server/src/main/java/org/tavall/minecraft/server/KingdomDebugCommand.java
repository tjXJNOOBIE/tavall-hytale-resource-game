package org.tavall.minecraft.server;

import org.tavall.minecraft.framework.game.ui.UiScreenKey;

import java.util.List;
import java.util.Optional;

public final class KingdomDebugCommand extends KingdomForwardingCommand {
    @Override
    protected String rootToken() {
        return "debug";
    }

    @Override
    protected String usage() {
        return "Usage: /kd debug";
    }

    @Override
    protected List<String> subcommands() {
        return List.of();
    }

    @Override
    protected Optional<UiScreenKey> defaultPage() {
        return Optional.of(UiScreenKey.DEBUG_NAVIGATOR);
    }
}
