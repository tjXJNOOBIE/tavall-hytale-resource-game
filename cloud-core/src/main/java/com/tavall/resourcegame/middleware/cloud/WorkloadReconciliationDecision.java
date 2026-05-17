package com.tavall.resourcegame.middleware.cloud;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public record WorkloadReconciliationDecision(
        UUID workloadId,
        WorkloadDesiredState desiredState,
        WorkloadActualState actualState,
        boolean actionRequired,
        Optional<CloudCommandType> commandType,
        String reason,
        Map<String, String> metadata
) {
    public WorkloadReconciliationDecision {
        commandType = commandType == null ? Optional.empty() : commandType;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
