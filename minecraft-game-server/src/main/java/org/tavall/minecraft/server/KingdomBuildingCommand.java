package org.tavall.minecraft.server;

import org.tavall.minecraft.framework.game.ui.UiScreenKey;

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
    protected Optional<UiScreenKey> defaultPage() {
        return Optional.of(UiScreenKey.BUILDING_DETAIL);
    }
}
