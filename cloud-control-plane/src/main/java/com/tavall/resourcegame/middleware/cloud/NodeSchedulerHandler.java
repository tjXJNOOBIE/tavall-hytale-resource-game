package com.tavall.resourcegame.middleware.cloud;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class NodeSchedulerHandler implements INodeSchedulerHandler, ICloudControlDomain {
    /**
     * Scores every node instead of returning the first match so dry-runs can explain both wins and rejections.
     */
    public NodeSchedulingDecision plan(WorkloadRequest request) {
        SchedulingPolicy policy = request.schedulingPolicy() == null ? SchedulingPolicy.defaultFor(request) : request.schedulingPolicy();
        List<NodeSchedulingCandidate> candidates = new ArrayList<>();
        List<NodeSchedulingCandidate> rejected = new ArrayList<>();
        for (CloudNode node : getCloudRepository().findNodes()) {
            NodeSchedulingCandidate candidate = evaluate(node, request, policy);
            if (candidate.eligible()) {
                candidates.add(candidate);
            } else {
                rejected.add(candidate);
            }
        }
        candidates.sort(Comparator.comparing(NodeSchedulingCandidate::score).reversed().thenComparing(candidate -> candidate.nodeId().toString()));
        Optional<NodeSchedulingCandidate> selected = candidates.stream().findFirst();
        return new NodeSchedulingDecision(
                selected.map(NodeSchedulingCandidate::nodeId),
                candidates,
                rejected,
                selected.isPresent(),
                selected.isPresent() ? "Selected node " + selected.get().nodeId() + "." : "No eligible cloud node.",
                java.util.Map.of()
        );
    }

    private NodeSchedulingCandidate evaluate(CloudNode node, WorkloadRequest request, SchedulingPolicy policy) {
        List<String> reasons = new ArrayList<>();
        List<String> rejectionReasons = new ArrayList<>();
        int score = 0;
        if (node.nodeStatus() == CloudNodeStatus.OFFLINE || node.nodeStatus() == CloudNodeStatus.FAILED) {
            rejectionReasons.add("node is not online");
        }
        if (policy.avoidDrainingNodes() && node.nodeStatus() == CloudNodeStatus.DRAINING) {
            rejectionReasons.add("node is draining");
        }
        if (policy.requireHealthyNode() && node.nodeStatus() == CloudNodeStatus.DEGRADED) {
            score -= 20;
            reasons.add("degraded node penalty");
        }
        if (!node.capabilities().containsAll(policy.requiredCapabilities())) {
            rejectionReasons.add("missing required capability");
        }
        if (policy.requiredArchitecture().isPresent() && node.architecture() != policy.requiredArchitecture().get()) {
            rejectionReasons.add("architecture mismatch");
        }
        if (node.cpuCores() < request.resourceLimits().cpuCores()) {
            rejectionReasons.add("insufficient cpu");
        }
        if (node.availableRamMb() < request.resourceLimits().ramMb()) {
            rejectionReasons.add("insufficient ram");
        }
        if (node.availableDiskGb() < request.resourceLimits().diskGb()) {
            rejectionReasons.add("insufficient disk");
        }
        score += Math.min(100, node.availableRamMb() / 256);
        score += Math.min(100, node.cpuCores() * 5);
        score += Math.min(100, node.availableDiskGb());
        if (policy.preferredRegion().isPresent() && policy.preferredRegion().get().equals(node.region())) {
            score += 50;
            reasons.add("preferred region");
        }
        if (node.capabilities().containsAll(policy.requiredCapabilities())) {
            score += policy.requiredCapabilities().size() * 10;
            reasons.add("capability match");
        }
        return new NodeSchedulingCandidate(node.nodeId(), rejectionReasons.isEmpty(), score, reasons, rejectionReasons, java.util.Map.of());
    }
}
