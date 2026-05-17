package com.tavall.resourcegame.middleware.cloud;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.time.Instant;

public interface IWorkloadRequestHandler extends IDependencyInjectableInterface {
    CloudWorkload createDesiredWorkload(WorkloadRequest request, Instant now);
}
