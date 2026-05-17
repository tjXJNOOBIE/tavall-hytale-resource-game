package com.tavall.resourcegame.dependency.interfaces;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;

import java.time.Instant;

public interface ICastleEconomySimulationService extends IDependencyInjectableInterface {
    void start();

    void shutdown();

    void runTick(Instant now);
}
