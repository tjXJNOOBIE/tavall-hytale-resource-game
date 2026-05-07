package com.tavall.hytale.resourcegame.distribution.node;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public record DistributedNode(
        String nodeId,
        DistributedNodeType nodeType,
        String hostname,
        String environment,
        String publicAddress,
        String privateAddress,
        long processId,
        Instant startedAt,
        Instant lastHeartbeatAt,
        Set<NodeCapability> capabilities,
        DistributedNodeStatus status,
        Map<String, String> metadata
) {
    public DistributedNode {
        if (nodeId == null || nodeId.isBlank()) {
            throw new IllegalArgumentException("nodeId is required.");
        }
        Objects.requireNonNull(nodeType, "nodeType");
        hostname = blankToUnknown(hostname);
        environment = blankToUnknown(environment);
        publicAddress = publicAddress == null ? "" : publicAddress;
        privateAddress = privateAddress == null ? "" : privateAddress;
        Objects.requireNonNull(startedAt, "startedAt");
        Objects.requireNonNull(lastHeartbeatAt, "lastHeartbeatAt");
        capabilities = capabilities == null ? Set.of() : Set.copyOf(capabilities);
        status = status == null ? DistributedNodeStatus.STARTING : status;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public boolean hasCapability(NodeCapability capability) {
        return capabilities.contains(capability);
    }

    public DistributedNode withHeartbeat(Instant heartbeatAt) {
        return new DistributedNode(
                nodeId,
                nodeType,
                hostname,
                environment,
                publicAddress,
                privateAddress,
                processId,
                startedAt,
                heartbeatAt,
                capabilities,
                status == DistributedNodeStatus.OFFLINE || status == DistributedNodeStatus.STARTING ? DistributedNodeStatus.ONLINE : status,
                metadata
        );
    }

    public DistributedNode withStatus(DistributedNodeStatus nextStatus, Instant heartbeatAt) {
        return new DistributedNode(
                nodeId,
                nodeType,
                hostname,
                environment,
                publicAddress,
                privateAddress,
                processId,
                startedAt,
                heartbeatAt == null ? lastHeartbeatAt : heartbeatAt,
                capabilities,
                nextStatus,
                metadata
        );
    }

    public DistributedNode withMetadata(Map<String, String> nextMetadata) {
        return new DistributedNode(
                nodeId,
                nodeType,
                hostname,
                environment,
                publicAddress,
                privateAddress,
                processId,
                startedAt,
                lastHeartbeatAt,
                capabilities,
                status,
                nextMetadata
        );
    }

    private static String blankToUnknown(String value) {
        return value == null || value.isBlank() ? "unknown" : value;
    }
}
