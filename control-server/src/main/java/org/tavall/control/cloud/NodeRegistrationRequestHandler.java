package org.tavall.control.cloud;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class NodeRegistrationRequestHandler implements INodeRegistrationRequestHandler, CloudControlDomain {
    public NodeRegistrationResult register(NodeRegistrationRequest request, Instant now) {
        Optional<JoinToken> joinToken = getJoinTokenValidationHandler().validate(request.joinToken(), now);
        if (joinToken.isEmpty()) {
            return NodeRegistrationResult.failed("Join token is missing, expired, consumed, or unknown.");
        }
        if (joinToken.get().allowedRegion().isPresent() && !joinToken.get().allowedRegion().get().equals(request.region())) {
            return NodeRegistrationResult.failed("Join token is not valid for region " + request.region() + ".");
        }

        UUID nodeId = UUID.randomUUID();
        UUID agentId = UUID.randomUUID();
        CloudNode node = new CloudNode(
                nodeId,
                request.hostname(),
                request.publicIp(),
                request.privateIp(),
                request.region(),
                request.datacenter(),
                request.provider(),
                request.metadata().getOrDefault("cpuModel", "unknown"),
                request.cpuCores(),
                request.totalRamMb(),
                request.availableRamMb(),
                request.totalDiskGb(),
                request.availableDiskGb(),
                request.osType(),
                request.architecture(),
                CloudNodeStatus.ONLINE,
                request.capabilities(),
                now,
                now,
                request.tags(),
                request.metadata()
        );
        NodeIdentity identity = new NodeIdentity(nodeId, agentId, Optional.empty(), Optional.of("issued:" + nodeId),
                "ACTIVE", now, Optional.empty(), Map.of("tokenHash", joinToken.get().tokenHash()));
        NodeAgentRuntime agent = new NodeAgentRuntime(agentId, nodeId, request.agentVersion(), now, now,
                request.supportedRuntimes(), Map.of());
        getCloudRepository().saveNode(node);
        getCloudRepository().saveIdentity(identity);
        getCloudRepository().saveAgent(agent);
        getCloudRepository().saveJoinToken(joinToken.get().consumed(now));
        return new NodeRegistrationResult(true, Optional.of(nodeId), Optional.of(agentId), Optional.of("issued:" + nodeId),
                "Node registered and schedulable.", Map.of("status", "ONLINE"));
    }
}
