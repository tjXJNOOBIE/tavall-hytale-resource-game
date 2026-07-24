package org.tavall.control.castle;

import org.tavall.dependency.IDependencyInjectableInterface;

public interface ICastleProximityPromptHandler extends IDependencyInjectableInterface {
    void start();

    void shutdown();
}
