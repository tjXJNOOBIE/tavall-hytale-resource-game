package org.tavall.control.cloud;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record NodeAgentRuntime(
        UUID agentId,
        UUID nodeId,
        String version,
        Instant startedAt,
        Instant lastHeartbeatAt,
        Set<SupportedRuntime> supportedRuntimes,
        Map<String, String> metadata
) {
    public NodeAgentRuntime {
        supportedRuntimes = supportedRuntimes == null ? Set.of() : Set.copyOf(supportedRuntimes);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
