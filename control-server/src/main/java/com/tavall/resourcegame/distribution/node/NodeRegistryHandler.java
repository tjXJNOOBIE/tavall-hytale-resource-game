package org.tavall.control.distribution.node;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class NodeRegistryHandler implements INodeRegistryHandler {
    private final Map<String, DistributedNode> nodesById = new ConcurrentHashMap<>();

    public DistributedNode registerNode(DistributedNode node) {
        nodesById.put(node.nodeId(), node);
        return node;
    }

    public Optional<DistributedNode> unregisterNode(String nodeId) {
        if (nodeId == null || nodeId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(nodesById.remove(nodeId));
    }

    public DistributedNode heartbeat(String nodeId) {
        return heartbeat(nodeId, Instant.now());
    }

    public DistributedNode heartbeat(String nodeId, Instant heartbeatAt) {
        DistributedNode updated = nodesById.compute(nodeId, (key, current) -> {
            if (current == null) {
                throw new IllegalArgumentException("Node is not registered: " + nodeId + ".");
            }
            return current.withHeartbeat(heartbeatAt);
        });
        if (updated == null) {
            throw new IllegalArgumentException("Node is not registered: " + nodeId + ".");
        }
        return updated;
    }

    public DistributedNode markNodeStatus(String nodeId, DistributedNodeStatus status) {
        return markNodeStatus(nodeId, status, Instant.now());
    }

    public DistributedNode markNodeStatus(String nodeId, DistributedNodeStatus status, Instant markedAt) {
        DistributedNode updated = nodesById.compute(nodeId, (key, current) -> {
            if (current == null) {
                throw new IllegalArgumentException("Node is not registered: " + nodeId + ".");
            }
            return current.withStatus(status, markedAt);
        });
        if (updated == null) {
            throw new IllegalArgumentException("Node is not registered: " + nodeId + ".");
        }
        return updated;
    }

    public Optional<DistributedNode> findNode(String nodeId) {
        return Optional.ofNullable(nodesById.get(nodeId));
    }

    public List<DistributedNode> getOnlineNodes() {
        return nodesById.values().stream()
                .filter(node -> node.status() == DistributedNodeStatus.ONLINE)
                .sorted(Comparator.comparing(DistributedNode::nodeId))
                .toList();
    }

    public List<DistributedNode> getNodesByCapability(NodeCapability capability) {
        return nodesById.values().stream()
                .filter(node -> node.hasCapability(capability))
                .sorted(Comparator.comparing(DistributedNode::nodeId))
                .toList();
    }

    public List<DistributedNode> allNodes() {
        return nodesById.values().stream()
                .sorted(Comparator.comparing(DistributedNode::nodeId))
                .toList();
    }

    public int markMissingHeartbeatsOffline(Duration maxHeartbeatAge, Instant now) {
        int[] changed = {0};
        nodesById.replaceAll((nodeId, node) -> {
            if (node.status() == DistributedNodeStatus.ONLINE
                    && Duration.between(node.lastHeartbeatAt(), now).compareTo(maxHeartbeatAge) > 0) {
                changed[0]++;
                return node.withStatus(DistributedNodeStatus.OFFLINE, node.lastHeartbeatAt());
            }
            return node;
        });
        return changed[0];
    }
}
