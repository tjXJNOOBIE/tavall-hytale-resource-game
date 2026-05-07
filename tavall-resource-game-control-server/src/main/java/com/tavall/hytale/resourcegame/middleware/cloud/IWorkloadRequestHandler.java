package com.tavall.hytale.resourcegame.middleware.cloud;

import com.tavall.hytale.resourcegame.dependency.IDependencyInjectableInterface;

import java.time.Instant;

public interface IWorkloadRequestHandler extends IDependencyInjectableInterface {
    CloudWorkload createDesiredWorkload(WorkloadRequest request, Instant now);
}
