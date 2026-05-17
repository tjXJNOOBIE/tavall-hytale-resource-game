package com.tavall.resourcegame.frontend.roblox;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.util.List;

public interface IRobloxKdCommandInputFormatterHandler extends IDependencyInjectableInterface {
    String rawKdInput(List<String> commandTokens);
}
