package com.tavall.hytale.resourcegame.middleware.cloud;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record NodeSchedulingCandidate(
        UUID nodeId,
        boolean eligible,
        int score,
        List<String> reasons,
        List<String> rejectionReasons,
        Map<String, String> metadata
) {
    public NodeSchedulingCandidate {
        reasons = reasons == null ? List.of() : List.copyOf(reasons);
        rejectionReasons = rejectionReasons == null ? List.of() : List.copyOf(rejectionReasons);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
