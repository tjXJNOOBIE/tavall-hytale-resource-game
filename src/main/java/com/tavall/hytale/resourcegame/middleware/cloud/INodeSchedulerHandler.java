package com.tavall.hytale.resourcegame.middleware.cloud;

import com.tavall.hytale.resourcegame.dependency.IDependencyInjectableInterface;

public interface INodeSchedulerHandler extends IDependencyInjectableInterface {
    NodeSchedulingDecision plan(WorkloadRequest request);
}
