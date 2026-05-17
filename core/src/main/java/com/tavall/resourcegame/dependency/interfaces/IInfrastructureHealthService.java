package com.tavall.resourcegame.dependency.interfaces;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;
import com.tavall.resourcegame.domain.InfrastructureHealthSnapshot;
import com.tavall.resourcegame.domain.InfrastructureMetricsSnapshot;

/**
 * Reports current cache and persistence operating modes for debug surfaces.
 */
public interface IInfrastructureHealthService extends IDependencyInjectableInterface {
    InfrastructureHealthSnapshot snapshot();

    InfrastructureMetricsSnapshot metricsSnapshot();
}
