package org.tavall.control.cloud;

import org.tavall.dependency.IDependencyInjectableConcrete;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

public record CloudAgentRuntimeConfig(
        URI controlPlaneUri,
        UUID nodeId,
        UUID agentId,
        String agentVersion,
        long pollIntervalMillis
) implements IDependencyInjectableConcrete {
    public CloudAgentRuntimeConfig {
        if (controlPlaneUri == null) {
            controlPlaneUri = URI.create("http://127.0.0.1:18080");
        }
        if (nodeId == null) {
            nodeId = new UUID(0L, 1L);
        }
        if (agentId == null) {
            agentId = new UUID(0L, 2L);
        }
        if (agentVersion == null || agentVersion.isBlank()) {
            agentVersion = "dev";
        }
        if (pollIntervalMillis <= 0) {
            pollIntervalMillis = 5000L;
        }
    }

    public static CloudAgentRuntimeConfig fromEnvironment(Map<String, String> environment) {
        return new CloudAgentRuntimeConfig(
                URI.create(environment.getOrDefault("TAVALL_CLOUD_CONTROL_PLANE_URL", "http://127.0.0.1:18080")),
                UUID.fromString(environment.getOrDefault("TAVALL_CLOUD_NODE_ID", "00000000-0000-0000-0000-000000000001")),
                UUID.fromString(environment.getOrDefault("TAVALL_CLOUD_AGENT_ID", "00000000-0000-0000-0000-000000000002")),
                environment.getOrDefault("TAVALL_CLOUD_AGENT_VERSION", "dev"),
                Long.parseLong(environment.getOrDefault("TAVALL_CLOUD_AGENT_POLL_INTERVAL_MILLIS", "5000"))
        );
    }
}
