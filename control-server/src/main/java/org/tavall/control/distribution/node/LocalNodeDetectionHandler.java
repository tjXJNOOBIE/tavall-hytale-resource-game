package org.tavall.control.distribution.node;

import java.net.InetAddress;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class LocalNodeDetectionHandler {
    public DistributedNode detectLocalNode(String nodeId, DistributedNodeType nodeType, Set<NodeCapability> capabilities) {
        Instant now = Instant.now();
        String hostname = detectHostname();
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("javaVersion", System.getProperty("java.version", ""));
        metadata.put("osName", System.getProperty("os.name", ""));
        metadata.put("userName", System.getProperty("user.name", ""));
        metadata.put("runtime", "local");
        return new DistributedNode(
                nodeId,
                nodeType,
                hostname,
                System.getenv().getOrDefault("RESOURCE_GAME_ENVIRONMENT", "local"),
                "127.0.0.1",
                "127.0.0.1",
                ProcessHandle.current().pid(),
                now,
                now,
                capabilities,
                DistributedNodeStatus.ONLINE,
                metadata
        );
    }

    private String detectHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception ignored) {
            return "localhost";
        }
    }
}
