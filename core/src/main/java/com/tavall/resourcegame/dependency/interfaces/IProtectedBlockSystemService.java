package com.tavall.resourcegame.dependency.interfaces;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;

public interface IProtectedBlockSystemService extends IDependencyInjectableInterface {
    void start();

    void shutdown();
}
