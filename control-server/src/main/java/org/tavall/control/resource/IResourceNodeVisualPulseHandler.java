package org.tavall.control.resource;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IResourceNodeVisualPulseHandler extends IDependencyInjectableInterface {
    void start();

    void shutdown();
}

