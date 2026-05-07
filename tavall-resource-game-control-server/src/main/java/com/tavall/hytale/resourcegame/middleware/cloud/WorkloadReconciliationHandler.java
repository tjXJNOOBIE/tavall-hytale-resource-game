package com.tavall.hytale.resourcegame.middleware.cloud;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public final class WorkloadReconciliationHandler implements IWorkloadReconciliationHandler, ICloudControlDomain {
    /**
     * Reconciliation is idempotent: desired/actual mismatches create one typed command, not repeated shell actions.
     */
    public WorkloadReconciliationDecision reconcile(UUID workloadId, UUID requestedBy, Instant now) {
        CloudWorkload workload = getCloudRepository().findWorkload(workloadId).orElseThrow();
        Optional<CloudCommandType> commandType = commandFor(workload);
        if (commandType.isEmpty()) {
            return new WorkloadReconciliationDecision(workloadId, workload.desiredState(), workload.actualState(), false,
                    Optional.empty(), "Desired and actual state already match.", java.util.Map.of());
        }
        UUID nodeId = workload.nodeId().orElseThrow();
        if (getCloudRepository().hasPendingCommand(nodeId, commandType.get(), workloadId)) {
            return new WorkloadReconciliationDecision(workloadId, workload.desiredState(), workload.actualState(), false,
                    commandType, "Pending command already exists for this workload action.", java.util.Map.of());
        }
        getCloudCommandCreationHandler().create(nodeId, commandType.get(), "{\"workloadId\":\"" + workloadId + "\"}", requestedBy, workloadId, now);
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
