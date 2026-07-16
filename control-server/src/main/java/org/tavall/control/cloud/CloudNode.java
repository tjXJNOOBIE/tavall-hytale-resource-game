package org.tavall.control.cloud;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record CloudNode(
        UUID nodeId,
        String hostname,
        String publicIp,
        String privateIp,
        String region,
        String datacenter,
        String provider,
        String cpuModel,
        int cpuCores,
        int totalRamMb,
        int availableRamMb,
        int totalDiskGb,
        int availableDiskGb,
        String osType,
        NodeArchitecture architecture,
        CloudNodeStatus nodeStatus,
        Set<CloudNodeCapability> capabilities,
        Instant lastHeartbeatAt,
        Instant registeredAt,
        Set<String> tags,
        Map<String, String> metadata
) {
    public CloudNode {
        capabilities = capabilities == null ? Set.of() : Set.copyOf(capabilities);
        tags = tags == null ? Set.of() : Set.copyOf(tags);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public CloudNode withStatus(CloudNodeStatus status, Instant heartbeatAt) {
        return new CloudNode(nodeId, hostname, publicIp, privateIp, region, datacenter, provider, cpuModel, cpuCores, totalRamMb,
                availableRamMb, totalDiskGb, availableDiskGb, osType, architecture, status, capabilities, heartbeatAt,
                registeredAt, tags, metadata);
    }
}
