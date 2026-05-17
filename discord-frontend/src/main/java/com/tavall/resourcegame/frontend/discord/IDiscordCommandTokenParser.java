package com.tavall.resourcegame.frontend.discord;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.util.List;

public interface IDiscordCommandTokenParser extends IDependencyInjectableInterface {
    List<String> parseCommandTokens(String rawCommand);
}
