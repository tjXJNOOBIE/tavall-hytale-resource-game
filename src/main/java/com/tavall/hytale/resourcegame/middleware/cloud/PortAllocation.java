package com.tavall.hytale.resourcegame.middleware.cloud;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record PortAllocation(
        UUID allocationId,
        UUID workloadId,
        UUID nodeId,
        PortProtocol protocol,
        int publicPort,
        int internalPort,
        PortAllocationStatus status,
        Instant createdAt,
        Map<String, String> metadata
) {
    public PortAllocation {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
