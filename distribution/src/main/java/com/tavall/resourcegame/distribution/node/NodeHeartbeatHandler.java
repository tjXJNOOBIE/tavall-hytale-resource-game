package com.tavall.resourcegame.distribution.node;

import com.tavall.resourcegame.distribution.IDistributionDomain;

import java.time.Duration;

public final class NodeHeartbeatHandler implements INodeHeartbeatHandler, IDistributionDomain {
    public DistributedNode heartbeat(String nodeId) {
        return getNodeRegistryHandler().heartbeat(nodeId, getDistributionClock().instant());
    }

    public int markStaleNodesOffline(Duration maxHeartbeatAge) {
        return getNodeRegistryHandler().markMissingHeartbeatsOffline(maxHeartbeatAge, getDistributionClock().instant());
    }
}
