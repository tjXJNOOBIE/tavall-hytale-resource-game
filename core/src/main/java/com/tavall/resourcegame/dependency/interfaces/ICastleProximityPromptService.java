package com.tavall.resourcegame.dependency.interfaces;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;

public interface ICastleProximityPromptService extends IDependencyInjectableInterface {
    void start();

    void shutdown();
}