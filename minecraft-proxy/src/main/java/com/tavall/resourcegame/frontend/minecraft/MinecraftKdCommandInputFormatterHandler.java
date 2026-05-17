package com.tavall.resourcegame.frontend.minecraft;

import com.tavall.resourcegame.shared.frontend.FrontendKdCommandInputFormatter;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.util.List;

public final class MinecraftKdCommandInputFormatterHandler implements IMinecraftKdCommandInputFormatterHandler, IDependencyInjectableConcrete {
    @Override
    public String rawKdInput(List<String> commandTokens) {
        return new FrontendKdCommandInputFormatter().rawKdInput(commandTokens);
    }
}
