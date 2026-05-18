package org.tavall.control.cloud;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public final class NodeMetricIngestionHandler implements INodeMetricIngestionHandler, ICloudControlDomain {
    private static final double WARNING_THRESHOLD = 90.0D;
    private static final double CRITICAL_THRESHOLD = 95.0D;

    public void ingest(NodeMetricSnapshot snapshot) {
        if (snapshot.nodeId() == null) {
            throw new IllegalArgumentException("Node metric snapshot requires a node id.");
        }
        getCloudRepository().saveNodeMetric(snapshot);
        alertIfCapacityHigh(snapshot.nodeId(), CloudAlertType.NODE_RAM_HIGH, snapshot.ramUsagePercent(), snapshot.collectedAt(), "ram");
        alertIfCapacityHigh(snapshot.nodeId(), CloudAlertType.NODE_DISK_HIGH, snapshot.diskUsagePercent(), snapshot.collectedAt(), "disk");
    }

    private void alertIfCapacityHigh(UUID nodeId, CloudAlertType alertType, double usagePercent, Instant now, String metricName) {
        if (usagePercent < WARNING_THRESHOLD) {
            return;
        }
        getCloudRepository().saveAlert(new CloudAlert(
                UUID.randomUUID(),
                alertType,
                usagePercent >= CRITICAL_THRESHOLD ? CloudAlertSeverity.CRITICAL : CloudAlertSeverity.WARNING,
                "node",
                nodeId.toString(),
                "Node " + metricName + " usage is %.2f%%.".formatted(usagePercent),
                CloudAlertState.ACTIVE,
                now,
                java.util.Optional.empty(),
                Map.of("usagePercent", String.valueOf(usagePercent))
        ));
    }
}
