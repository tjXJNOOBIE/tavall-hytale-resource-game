package org.tavall.control.cloud;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record CloudAgentHeartbeatPayload(
        UUID nodeId,
        UUID agentId,
        String version,
        Instant heartbeatAt,
        Map<String, String> metadata
) {
    public CloudAgentHeartbeatPayload {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
