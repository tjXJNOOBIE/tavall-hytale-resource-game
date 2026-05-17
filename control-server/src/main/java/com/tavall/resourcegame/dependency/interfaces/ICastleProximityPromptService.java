package com.tavall.resourcegame.dependency.interfaces;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface ICastleProximityPromptService extends IDependencyInjectableInterface {
    void start();

    void shutdown();
}