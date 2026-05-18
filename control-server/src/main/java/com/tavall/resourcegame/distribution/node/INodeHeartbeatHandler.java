package org.tavall.control.distribution.node;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.time.Duration;

public interface INodeHeartbeatHandler extends IDependencyInjectableInterface {
    DistributedNode heartbeat(String nodeId);

    int markStaleNodesOffline(Duration maxHeartbeatAge);
}
