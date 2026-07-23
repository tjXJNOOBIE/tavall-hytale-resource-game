package org.tavall.control.castle;

import org.tavall.dependency.IDependencyInjectableInterface;

import java.time.Instant;

public interface ICastleEconomySimulationHandler extends IDependencyInjectableInterface {
    void start();

    void shutdown();

    void runTick(Instant now);
}

