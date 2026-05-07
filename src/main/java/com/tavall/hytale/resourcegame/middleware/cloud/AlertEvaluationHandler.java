package com.tavall.hytale.resourcegame.middleware.cloud;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class AlertEvaluationHandler {
    private final InMemoryCloudRepository repository;

    public AlertEvaluationHandler(InMemoryCloudRepository repository) {
        this.repository = repository;
    }

    public Optional<CloudAlert> evaluateHeartbeat(CloudNode node, Instant now, Duration threshold) {
        if (node.lastHeartbeatAt().plus(threshold).isAfter(now)) {
            return Optional.empty();
        }
        CloudAlert alert = new CloudAlert(UUID.randomUUID(), CloudAlertType.NODE_MISSED_HEARTBEAT,
                CloudAlertSeverity.CRITICAL, "NODE", node.nodeId().toString(),
                "Node missed heartbeat threshold.", CloudAlertState.ACTIVE, now, Optional.empty(), Map.of());
        repository.saveAlert(alert);
        repository.saveNode(node.withStatus(CloudNodeStatus.OFFLINE, node.lastHeartbeatAt()));
        return Optional.of(alert);
    }
}
