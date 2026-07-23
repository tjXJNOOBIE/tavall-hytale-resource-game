package org.tavall.control.distribution.node;

import org.tavall.dependency.IDependencyInjectableInterface;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface INodeRegistryHandler extends IDependencyInjectableInterface {
    DistributedNode registerNode(DistributedNode node);

    Optional<DistributedNode> unregisterNode(String nodeId);

    DistributedNode heartbeat(String nodeId);

    DistributedNode heartbeat(String nodeId, Instant heartbeatAt);

    DistributedNode markNodeStatus(String nodeId, DistributedNodeStatus status);

    DistributedNode markNodeStatus(String nodeId, DistributedNodeStatus status, Instant markedAt);

    Optional<DistributedNode> findNode(String nodeId);

    List<DistributedNode> getOnlineNodes();

    List<DistributedNode> getNodesByCapability(NodeCapability capability);

    List<DistributedNode> allNodes();

    int markMissingHeartbeatsOffline(Duration maxHeartbeatAge, Instant now);
}
