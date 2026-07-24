package org.tavall.control.protection;

import org.tavall.dependency.IDependencyInjectableInterface;

public interface IProtectedBlockSystemHandler extends IDependencyInjectableInterface {
    void start();

    void shutdown();
}

