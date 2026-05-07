package com.tavall.hytale.resourcegame.middleware.cloud;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public final class WorkloadReconciliationHandler {
    private final InMemoryCloudRepository repository;
    private final CloudCommandCreationHandler commandCreationHandler;

    public WorkloadReconciliationHandler(InMemoryCloudRepository repository, CloudCommandCreationHandler commandCreationHandler) {
        this.repository = repository;
        this.commandCreationHandler = commandCreationHandler;
    }

    public WorkloadReconciliationDecision reconcile(UUID workloadId, UUID requestedBy, Instant now) {
        CloudWorkload workload = repository.findWorkload(workloadId).orElseThrow();
        Optional<CloudCommandType> commandType = commandFor(workload);
        if (commandType.isEmpty()) {
            return new WorkloadReconciliationDecision(workloadId, workload.desiredState(), workload.actualState(), false,
                    Optional.empty(), "Desired and actual state already match.", java.util.Map.of());
        }
        UUID nodeId = workload.nodeId().orElseThrow();
        if (repository.hasPendingCommand(nodeId, commandType.get(), workloadId)) {
            return new WorkloadReconciliationDecision(workloadId, workload.desiredState(), workload.actualState(), false,
                    commandType, "Pending command already exists for this workload action.", java.util.Map.of());
        }
        commandCreationHandler.create(nodeId, commandType.get(), "{\"workloadId\":\"" + workloadId + "\"}", requestedBy, workloadId, now);
        return new WorkloadReconciliationDecision(workloadId, workload.desiredState(), workload.actualState(), true,
                commandType, "Created " + commandType.get() + " command.", java.util.Map.of());
    }

    private Optional<CloudCommandType> commandFor(CloudWorkload workload) {
        if (workload.desiredState() == WorkloadDesiredState.RUNNING && workload.actualState() != WorkloadActualState.RUNNING) {
            return Optional.of(CloudCommandType.START_WORKLOAD);
        }
        if (workload.desiredState() == WorkloadDesiredState.STOPPED && workload.actualState() == WorkloadActualState.RUNNING) {
            return Optional.of(CloudCommandType.STOP_WORKLOAD);
        }
        if (workload.desiredState() == WorkloadDesiredState.RESTARTING) {
            return Optional.of(CloudCommandType.RESTART_WORKLOAD);
        }
        if (workload.desiredState() == WorkloadDesiredState.DELETED && workload.actualState() != WorkloadActualState.DELETED) {
            return Optional.of(CloudCommandType.DELETE_WORKLOAD);
        }
        return Optional.empty();
    }
}
