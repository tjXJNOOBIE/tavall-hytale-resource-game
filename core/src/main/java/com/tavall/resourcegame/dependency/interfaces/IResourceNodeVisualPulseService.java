package com.tavall.resourcegame.dependency.interfaces;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;

public interface IResourceNodeVisualPulseService extends IDependencyInjectableInterface {
    void start();

    void shutdown();
}
