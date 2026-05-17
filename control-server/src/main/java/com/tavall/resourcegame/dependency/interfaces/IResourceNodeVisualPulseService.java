package com.tavall.resourcegame.dependency.interfaces;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IResourceNodeVisualPulseService extends IDependencyInjectableInterface {
    void start();

    void shutdown();
}
