package com.tavall.resourcegame.frontend.discord;

import com.tavall.resourcegame.shared.frontend.FrontendKdCommandInputFormatter;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.util.List;

public final class DiscordKdCommandInputFormatterHandler implements IDiscordKdCommandInputFormatterHandler, IDependencyInjectableConcrete {
    @Override
    public String rawKdInput(List<String> commandTokens) {
        return new FrontendKdCommandInputFormatter().rawKdInput(commandTokens);
    }
}
