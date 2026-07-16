package org.tavall.control.castle;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.time.Instant;

public interface ICastleEconomySimulationHandler extends IDependencyInjectableInterface {
    void start();

    void shutdown();

    void runTick(Instant now);
}

