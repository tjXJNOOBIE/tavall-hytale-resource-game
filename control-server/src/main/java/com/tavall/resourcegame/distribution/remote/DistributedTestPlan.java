package com.tavall.resourcegame.distribution.remote;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

public record DistributedTestPlan(
        String planId,
        RemoteTarget target,
        Path artifactDirectory,
        boolean verifyRedis,
        boolean verifyPostgres,
        boolean verifyHytale,
        Map<String, String> metadata
) {
    public DistributedTestPlan {
        planId = planId == null || planId.isBlank() ? UUID.randomUUID().toString() : planId;
        if (target == null) {
            target = RemoteTarget.local();
        }
        artifactDirectory = artifactDirectory == null ? Path.of("bot-logs", "distributed") : artifactDirectory;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
