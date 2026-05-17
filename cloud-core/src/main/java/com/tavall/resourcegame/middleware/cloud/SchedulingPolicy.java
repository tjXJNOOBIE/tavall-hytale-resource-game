package com.tavall.resourcegame.middleware.cloud;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public record SchedulingPolicy(
        Optional<String> preferredRegion,
        Set<CloudNodeCapability> requiredCapabilities,
        Optional<NodeArchitecture> requiredArchitecture,
        boolean avoidDrainingNodes,
        boolean requireHealthyNode,
        boolean preferLowLoad,
        Map<String, String> metadata
) {
    public SchedulingPolicy {
        preferredRegion = preferredRegion == null ? Optional.empty() : preferredRegion;
        requiredCapabilities = requiredCapabilities == null ? Set.of() : Set.copyOf(requiredCapabilities);
        requiredArchitecture = requiredArchitecture == null ? Optional.empty() : requiredArchitecture;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static SchedulingPolicy defaultFor(WorkloadRequest request) {
        return new SchedulingPolicy(
                request.preferredRegion(),
                request.requiredCapabilities(),
                request.requiredArchitecture(),
                true,
                true,
                true,
                Map.of()
        );
    }
}
