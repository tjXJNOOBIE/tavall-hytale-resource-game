package com.tavall.resourcegame.middleware.cloud;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;

import java.time.Instant;

public interface IWorkloadRequestHandler extends IDependencyInjectableInterface {
    CloudWorkload createDesiredWorkload(WorkloadRequest request, Instant now);
}
