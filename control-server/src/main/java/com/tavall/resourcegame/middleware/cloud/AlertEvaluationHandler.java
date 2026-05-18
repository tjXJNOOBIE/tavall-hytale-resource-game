package org.tavall.control.cloud;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class AlertEvaluationHandler implements IAlertEvaluationHandler, ICloudControlDomain {
    public Optional<CloudAlert> evaluateHeartbeat(CloudNode node, Instant now, Duration threshold) {
        if (node.lastHeartbeatAt().plus(threshold).isAfter(now)) {
            return Optional.empty();
        }
        CloudAlert alert = new CloudAlert(UUID.randomUUID(), CloudAlertType.NODE_MISSED_HEARTBEAT,
                CloudAlertSeverity.CRITICAL, "NODE", node.nodeId().toString(),
                "Node missed heartbeat threshold.", CloudAlertState.ACTIVE, now, Optional.empty(), Map.of());
        getCloudRepository().saveAlert(alert);
        getCloudRepository().saveNode(node.withStatus(CloudNodeStatus.OFFLINE, node.lastHeartbeatAt()));
        return Optional.of(alert);
    }
}
