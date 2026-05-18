package org.tavall.minecraft.server;

import org.tavall.api.minecraft.ui.UiPageType;

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
    protected Optional<UiPageType> defaultPage() {
        return Optional.of(UiPageType.DEBUG_NAVIGATOR);
    }
}
