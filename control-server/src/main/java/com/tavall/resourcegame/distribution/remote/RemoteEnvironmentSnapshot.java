package org.tavall.control.distribution.remote;

import java.time.Instant;
import java.util.Map;

public record RemoteEnvironmentSnapshot(
        String targetId,
        String hostname,
        String operatingSystem,
        String javaVersion,
        String userName,
        boolean reachable,
        Instant probedAt,
        Map<String, String> metadata
) {
    public RemoteEnvironmentSnapshot {
        hostname = hostname == null ? "" : hostname;
        operatingSystem = operatingSystem == null ? "" : operatingSystem;
        javaVersion = javaVersion == null ? "" : javaVersion;
        userName = userName == null ? "" : userName;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
