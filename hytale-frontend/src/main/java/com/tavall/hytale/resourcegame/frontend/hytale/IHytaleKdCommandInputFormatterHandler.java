package com.tavall.hytale.resourcegame.frontend.hytale;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.util.List;

public interface IHytaleKdCommandInputFormatterHandler extends IDependencyInjectableInterface {
    String rawKdInput(List<String> commandTokens);
}
