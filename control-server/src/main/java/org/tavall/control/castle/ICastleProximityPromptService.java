package org.tavall.control.castle;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface ICastleProximityPromptService extends IDependencyInjectableInterface {
    void start();

    void shutdown();
}
