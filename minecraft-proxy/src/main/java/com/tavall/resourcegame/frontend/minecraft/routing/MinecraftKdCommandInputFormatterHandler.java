package org.tavall.minecraft.routing;

import org.tavall.api.minecraft.frontend.FrontendKdCommandInputFormatter;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.util.List;

public final class MinecraftKdCommandInputFormatterHandler implements IMinecraftKdCommandInputFormatterHandler, IDependencyInjectableConcrete {
    @Override
    public String rawKdInput(List<String> commandTokens) {
        return new FrontendKdCommandInputFormatter().rawKdInput(commandTokens);
    }
}
