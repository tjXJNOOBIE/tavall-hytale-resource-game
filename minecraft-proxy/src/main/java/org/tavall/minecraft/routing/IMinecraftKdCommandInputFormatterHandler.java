package org.tavall.minecraft.routing;

import org.tavall.dependency.IDependencyInjectableInterface;

import java.util.List;

public interface IMinecraftKdCommandInputFormatterHandler extends IDependencyInjectableInterface {
    String rawKdInput(List<String> commandTokens);
}
