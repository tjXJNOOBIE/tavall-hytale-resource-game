package org.tavall.control.cloud;

import org.tavall.dependency.IDependencyInjectableInterface;

public interface INodeSchedulerHandler extends IDependencyInjectableInterface {
    NodeSchedulingDecision plan(WorkloadRequest request);
}
