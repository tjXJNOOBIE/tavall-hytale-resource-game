package org.tavall.control.cloud;

import java.util.Map;
import java.util.UUID;

public final class WorkloadMetricIngestionHandler implements IWorkloadMetricIngestionHandler, CloudControlDomain {
    private static final int CRASH_LOOP_RESTART_THRESHOLD = 3;

    public void ingest(WorkloadMetricSnapshot snapshot) {
        if (snapshot.workloadId() == null || snapshot.nodeId() == null) {
            throw new IllegalArgumentException("Workload metric snapshot requires workload and node ids.");
        }
        getCloudRepository().saveWorkloadMetric(snapshot);
        if (snapshot.restartCount() < CRASH_LOOP_RESTART_THRESHOLD && snapshot.healthStatus() != WorkloadHealthStatus.FAILED) {
            return;
        }
        getCloudRepository().saveAlert(new CloudAlert(
                UUID.randomUUID(),
                CloudAlertType.WORKLOAD_CRASH_LOOP,
                CloudAlertSeverity.CRITICAL,
                "workload",
                snapshot.workloadId().toString(),
                "Workload restart count or failed health indicates a crash loop.",
                CloudAlertState.ACTIVE,
                snapshot.collectedAt(),
                java.util.Optional.empty(),
                Map.of("restartCount", String.valueOf(snapshot.restartCount()))
        ));
    }
}
