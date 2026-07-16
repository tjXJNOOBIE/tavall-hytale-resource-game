package org.tavall.control.cloud;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class WorkloadRequestHandler implements IWorkloadRequestHandler, CloudControlDomain {
    public CloudWorkload createDesiredWorkload(WorkloadRequest request, Instant now) {
        NodeSchedulingDecision decision = getNodeSchedulerHandler().plan(request);
        if (!decision.success()) {
            throw new IllegalStateException(decision.message());
        }
        CloudWorkload workload = new CloudWorkload(
                UUID.randomUUID(),
                request.workloadType(),
                Optional.empty(),
                request.name(),
                request.metadata().getOrDefault("image", request.workloadType().name().toLowerCase()),
                request.metadata().getOrDefault("version", "latest"),
                List.of(),
                request.environmentVariables(),
                request.secretRefs(),
                java.util.Set.of(),
                request.resourceLimits(),
                WorkloadDesiredState.RUNNING,
                WorkloadActualState.PENDING,
                WorkloadHealthStatus.UNKNOWN,
                now,
                now,
                request.metadata()
        ).scheduledOn(decision.selectedNodeId().orElseThrow(), now);
        getCloudRepository().saveWorkload(workload);
        return workload;
    }
}
