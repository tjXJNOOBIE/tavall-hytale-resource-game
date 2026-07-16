package org.tavall.control.distribution.node;

import org.tavall.control.distribution.DistributionDomain;

import java.time.Duration;

public final class NodeHeartbeatHandler implements INodeHeartbeatHandler, DistributionDomain {
    public DistributedNode heartbeat(String nodeId) {
        return getNodeRegistryHandler().heartbeat(nodeId, getDistributionClock().instant());
    }

    public int markStaleNodesOffline(Duration maxHeartbeatAge) {
        return getNodeRegistryHandler().markMissingHeartbeatsOffline(maxHeartbeatAge, getDistributionClock().instant());
    }
}
