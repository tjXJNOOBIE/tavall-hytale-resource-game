package com.tavall.resourcegame.frontend.discord;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.util.List;

public interface IDiscordKdCommandInputFormatterHandler extends IDependencyInjectableInterface {
    String rawKdInput(List<String> commandTokens);
}
