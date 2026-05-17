package com.tavall.resourcegame.middleware.cloud;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;

public interface INodeSchedulerHandler extends IDependencyInjectableInterface {
    NodeSchedulingDecision plan(WorkloadRequest request);
}
