package org.tavall.control.runtime;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import org.tavall.control.domain.InfrastructureHealthSnapshot;
import org.tavall.control.domain.InfrastructureMetricsSnapshot;

/**
 * Reports current cache and persistence operating modes for debug surfaces.
 */
public interface IInfrastructureHealthHandler extends IDependencyInjectableInterface {
    InfrastructureHealthSnapshot snapshot();

    InfrastructureMetricsSnapshot metricsSnapshot();
}

