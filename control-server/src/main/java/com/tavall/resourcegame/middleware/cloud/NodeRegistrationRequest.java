package org.tavall.control.cloud;

import java.util.Map;
import java.util.Set;

public record NodeRegistrationRequest(
        String joinToken,
        String hostname,
        String publicIp,
        String privateIp,
        String region,
        String datacenter,
        String provider,
        String osType,
        NodeArchitecture architecture,
        String agentVersion,
        int cpuCores,
        int totalRamMb,
        int availableRamMb,
        int totalDiskGb,
        int availableDiskGb,
        Set<CloudNodeCapability> capabilities,
        Set<SupportedRuntime> supportedRuntimes,
        Set<String> tags,
        Map<String, String> metadata
) {
    public NodeRegistrationRequest {
        capabilities = capabilities == null ? Set.of() : Set.copyOf(capabilities);
        supportedRuntimes = supportedRuntimes == null ? Set.of() : Set.copyOf(supportedRuntimes);
        tags = tags == null ? Set.of() : Set.copyOf(tags);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
