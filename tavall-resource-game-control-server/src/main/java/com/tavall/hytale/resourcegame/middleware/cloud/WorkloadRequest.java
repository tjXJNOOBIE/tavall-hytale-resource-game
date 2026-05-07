package com.tavall.hytale.resourcegame.middleware.cloud;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public record WorkloadRequest(
        CloudWorkloadType workloadType,
        String name,
        Optional<String> preferredRegion,
        Set<CloudNodeCapability> requiredCapabilities,
        Optional<NodeArchitecture> requiredArchitecture,
        ResourceLimits resourceLimits,
        Map<String, String> environmentVariables,
        Set<String> secretRefs,
        SchedulingPolicy schedulingPolicy,
        Map<String, String> metadata
) {
    public WorkloadRequest {
        preferredRegion = preferredRegion == null ? Optional.empty() : preferredRegion;
        requiredCapabilities = requiredCapabilities == null ? Set.of() : Set.copyOf(requiredCapabilities);
        requiredArchitecture = requiredArchitecture == null ? Optional.empty() : requiredArchitecture;
        environmentVariables = environmentVariables == null ? Map.of() : Map.copyOf(environmentVariables);
        secretRefs = secretRefs == null ? Set.of() : Set.copyOf(secretRefs);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
