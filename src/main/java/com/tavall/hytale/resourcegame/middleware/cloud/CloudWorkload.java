package com.tavall.hytale.resourcegame.middleware.cloud;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public record CloudWorkload(
        UUID workloadId,
        CloudWorkloadType workloadType,
        Optional<UUID> nodeId,
        String name,
        String image,
        String version,
        List<Integer> ports,
        Map<String, String> environmentVariables,
        Set<String> secretRefs,
        Set<String> volumeMounts,
        ResourceLimits resourceLimits,
        WorkloadDesiredState desiredState,
        WorkloadActualState actualState,
        WorkloadHealthStatus healthStatus,
        Instant createdAt,
        Instant updatedAt,
        Map<String, String> metadata
) {
    public CloudWorkload {
        nodeId = nodeId == null ? Optional.empty() : nodeId;
        ports = ports == null ? List.of() : List.copyOf(ports);
        environmentVariables = environmentVariables == null ? Map.of() : Map.copyOf(environmentVariables);
        secretRefs = secretRefs == null ? Set.of() : Set.copyOf(secretRefs);
        volumeMounts = volumeMounts == null ? Set.of() : Set.copyOf(volumeMounts);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public CloudWorkload scheduledOn(UUID selectedNodeId, Instant now) {
        return new CloudWorkload(workloadId, workloadType, Optional.of(selectedNodeId), name, image, version, ports,
                environmentVariables, secretRefs, volumeMounts, resourceLimits, desiredState, actualState, healthStatus,
                createdAt, now, metadata);
    }

    public CloudWorkload withActualState(WorkloadActualState state, WorkloadHealthStatus health, Instant now) {
        return new CloudWorkload(workloadId, workloadType, nodeId, name, image, version, ports, environmentVariables,
                secretRefs, volumeMounts, resourceLimits, desiredState, state, health, createdAt, now, metadata);
    }
}
