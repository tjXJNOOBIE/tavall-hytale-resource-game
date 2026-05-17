package com.tavall.resourcegame.middleware.cloud;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public record NodeSchedulingDecision(
        Optional<UUID> selectedNodeId,
        List<NodeSchedulingCandidate> candidates,
        List<NodeSchedulingCandidate> rejectedCandidates,
        boolean success,
        String message,
        Map<String, String> metadata
) {
    public NodeSchedulingDecision {
        selectedNodeId = selectedNodeId == null ? Optional.empty() : selectedNodeId;
        candidates = candidates == null ? List.of() : List.copyOf(candidates);
        rejectedCandidates = rejectedCandidates == null ? List.of() : List.copyOf(rejectedCandidates);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
