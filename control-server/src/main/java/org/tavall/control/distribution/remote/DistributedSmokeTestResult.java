package org.tavall.control.distribution.remote;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;

public record DistributedSmokeTestResult(
        String planId,
        String targetId,
        boolean successful,
        RemoteEnvironmentSnapshot environmentSnapshot,
        Map<String, Boolean> checks,
        Path artifactPath,
        Instant startedAt,
        Instant completedAt,
        Map<String, String> metadata
) {
    public DistributedSmokeTestResult {
        checks = checks == null ? Map.of() : Map.copyOf(checks);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
