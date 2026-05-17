package com.tavall.resourcegame.frontend.minecraft.routing;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.util.List;

public interface IMinecraftKdCommandInputFormatterHandler extends IDependencyInjectableInterface {
    String rawKdInput(List<String> commandTokens);
}
