package org.tavall.control.resource;

import org.tavall.dependency.IDependencyInjectableInterface;

public interface IResourceNodeVisualPulseHandler extends IDependencyInjectableInterface {
    void start();

    void shutdown();
}

