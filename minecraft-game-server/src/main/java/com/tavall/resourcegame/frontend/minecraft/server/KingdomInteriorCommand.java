package com.tavall.resourcegame.frontend.minecraft.server;

import com.tavall.resourcegame.ui.UiPageType;

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
    protected Optional<UiPageType> defaultPage() {
        return Optional.of(UiPageType.INTERIOR_MAIN);
    }
}
