package com.tavall.hytale.resourcegame.middleware.cloud;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public final class PortAllocationHandler implements IPortAllocationHandler, ICloudControlDomain {
    public PortAllocation allocate(UUID workloadId, UUID nodeId, PortProtocol protocol, int publicPort, int internalPort, Instant now) {
        if (getCloudRepository().portInUse(nodeId, protocol, publicPort)) {
            throw new IllegalStateException("Port " + publicPort + "/" + protocol + " is already allocated on node " + nodeId + ".");
        }
        PortAllocation allocation = new PortAllocation(UUID.randomUUID(), workloadId, nodeId, protocol, publicPort, internalPort,
                PortAllocationStatus.RESERVED, now, Map.of());
        getCloudRepository().savePort(allocation);
        return allocation;
    }
}
