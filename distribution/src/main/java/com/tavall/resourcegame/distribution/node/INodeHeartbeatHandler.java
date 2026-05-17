package com.tavall.resourcegame.distribution.node;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;

import java.time.Duration;

public interface INodeHeartbeatHandler extends IDependencyInjectableInterface {
    DistributedNode heartbeat(String nodeId);

    int markStaleNodesOffline(Duration maxHeartbeatAge);
}
