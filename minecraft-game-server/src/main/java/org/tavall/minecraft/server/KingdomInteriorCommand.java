package org.tavall.minecraft.server;

import org.tavall.minecraft.framework.game.ui.UiScreenKey;

import java.util.List;
import java.util.Optional;

public final class KingdomInteriorCommand extends KingdomForwardingCommand {
    @Override
    protected String rootToken() {
        return "interior";
    }

    @Override
    protected String usage() {
        return "Usage: /kd interior [exit|add|generate|rebuild|regen|move|delete]";
    }

    @Override
    protected List<String> subcommands() {
        return List.of("exit", "add", "generate", "rebuild", "regen", "move", "delete");
    }

    @Override
    protected Optional<UiScreenKey> defaultPage() {
        return Optional.of(UiScreenKey.INTERIOR_MAIN);
    }
}
