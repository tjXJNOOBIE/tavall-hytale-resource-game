package org.tavall.minecraft.server;

import org.tavall.minecraft.framework.game.ui.UiScreenKey;

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
    protected Optional<UiScreenKey> defaultPage() {
        return Optional.of(UiScreenKey.DEBUG_WORLD);
    }
}
