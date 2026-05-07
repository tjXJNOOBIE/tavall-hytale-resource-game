package com.tavall.hytale.resourcegame.middleware.cloud;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class WorkloadRequestHandler {
    private final InMemoryCloudRepository repository;
    private final NodeSchedulerHandler schedulerHandler;

    public WorkloadRequestHandler(InMemoryCloudRepository repository, NodeSchedulerHandler schedulerHandler) {
        this.repository = repository;
        this.schedulerHandler = schedulerHandler;
    }

    public CloudWorkload createDesiredWorkload(WorkloadRequest request, Instant now) {
        NodeSchedulingDecision decision = schedulerHandler.plan(request);
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
        repository.saveWorkload(workload);
        return workload;
    }
}
