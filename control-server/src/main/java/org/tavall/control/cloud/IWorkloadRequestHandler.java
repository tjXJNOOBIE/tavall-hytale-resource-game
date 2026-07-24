package org.tavall.control.cloud;

import org.tavall.dependency.IDependencyInjectableInterface;

import java.time.Instant;

public interface IWorkloadRequestHandler extends IDependencyInjectableInterface {
    CloudWorkload createDesiredWorkload(WorkloadRequest request, Instant now);
}
