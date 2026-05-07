package com.tavall.hytale.resourcegame.middleware.cloud;

import java.time.Instant;
import java.util.UUID;

public final class NodeHeartbeatHandler {
    private final InMemoryCloudRepository repository;

    public NodeHeartbeatHandler(InMemoryCloudRepository repository) {
        this.repository = repository;
    }

    public boolean heartbeat(UUID nodeId, Instant now) {
        return repository.findNode(nodeId)
                .map(node -> {
                    repository.saveNode(node.withStatus(CloudNodeStatus.ONLINE, now));
                    return true;
                })
                .orElse(false);
    }
}
