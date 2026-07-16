package org.tavall.control.cloud;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface INodeSchedulerHandler extends IDependencyInjectableInterface {
    NodeSchedulingDecision plan(WorkloadRequest request);
}
