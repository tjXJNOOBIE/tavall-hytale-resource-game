package com.tavall.resourcegame.dependency.interfaces;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.time.Instant;

public interface ICastleEconomySimulationService extends IDependencyInjectableInterface {
    void start();

    void shutdown();

    void runTick(Instant now);
}
