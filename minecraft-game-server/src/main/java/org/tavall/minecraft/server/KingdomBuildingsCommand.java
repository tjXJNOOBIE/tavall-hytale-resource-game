package org.tavall.minecraft.server;

import org.tavall.minecraft.framework.game.ui.UiScreenKey;

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
    protected Optional<UiScreenKey> defaultPage() {
        return Optional.of(UiScreenKey.CASTLE_BUILDINGS);
    }
}
