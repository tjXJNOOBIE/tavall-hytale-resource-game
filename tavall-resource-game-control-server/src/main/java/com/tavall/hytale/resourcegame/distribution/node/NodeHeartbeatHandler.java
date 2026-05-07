package com.tavall.hytale.resourcegame.distribution.node;

import java.time.Clock;
import java.time.Duration;
import java.util.Objects;

public final class NodeHeartbeatHandler {
    private final NodeRegistryHandler registryHandler;
    private final Clock clock;

    public NodeHeartbeatHandler(NodeRegistryHandler registryHandler, Clock clock) {
        this.registryHandler = Objects.requireNonNull(registryHandler, "registryHandler");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public DistributedNode heartbeat(String nodeId) {
        return registryHandler.heartbeat(nodeId, clock.instant());
    }

    public int markStaleNodesOffline(Duration maxHeartbeatAge) {
        return registryHandler.markMissingHeartbeatsOffline(maxHeartbeatAge, clock.instant());
    }
}
