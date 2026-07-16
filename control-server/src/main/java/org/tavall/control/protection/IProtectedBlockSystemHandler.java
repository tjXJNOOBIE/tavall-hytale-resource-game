package org.tavall.control.protection;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IProtectedBlockSystemHandler extends IDependencyInjectableInterface {
    void start();

    void shutdown();
}

