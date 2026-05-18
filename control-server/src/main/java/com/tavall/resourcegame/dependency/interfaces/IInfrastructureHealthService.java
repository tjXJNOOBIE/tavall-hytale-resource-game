package org.tavall.control.dependency.interfaces;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import org.tavall.control.domain.InfrastructureHealthSnapshot;
import org.tavall.control.domain.InfrastructureMetricsSnapshot;

/**
 * Reports current cache and persistence operating modes for debug surfaces.
 */
public interface IInfrastructureHealthService extends IDependencyInjectableInterface {
    InfrastructureHealthSnapshot snapshot();

    InfrastructureMetricsSnapshot metricsSnapshot();
}
