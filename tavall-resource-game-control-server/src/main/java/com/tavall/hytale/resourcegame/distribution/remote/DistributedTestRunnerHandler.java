package com.tavall.hytale.resourcegame.distribution.remote;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tavall.hytale.resourcegame.distribution.health.RemoteHealthCheckHandler;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class DistributedTestRunnerHandler {
    private final RemoteEnvironmentProbeHandler probeHandler;
    private final RemoteHealthCheckHandler healthCheckHandler;
    private final ObjectMapper objectMapper;

    public DistributedTestRunnerHandler(RemoteEnvironmentProbeHandler probeHandler, RemoteHealthCheckHandler healthCheckHandler) {
        this.probeHandler = Objects.requireNonNull(probeHandler, "probeHandler");
        this.healthCheckHandler = Objects.requireNonNull(healthCheckHandler, "healthCheckHandler");
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(new Jdk8Module());
    }

    public DistributedSmokeTestResult runDistributedSmokeTest(DistributedTestPlan plan) {
        Instant startedAt = Instant.now();
        RemoteEnvironmentSnapshot snapshot = plan.target().localTarget()
                ? probeHandler.detectLocalEnvironment()
                : probeHandler.probeRemoteEnvironment(plan.target());
        Map<String, Boolean> checks = new LinkedHashMap<>();
        if (plan.verifyRedis()) {
            checks.put("redis", healthCheckHandler.verifyRedisConnectivity(plan.target()));
        }
        if (plan.verifyPostgres()) {
            checks.put("postgres", healthCheckHandler.verifyPostgresConnectivity(plan.target()));
        }
        if (plan.verifyHytale()) {
            checks.put("hytale", healthCheckHandler.verifyHytaleServerReachable(plan.target()));
        }
        boolean successful = snapshot.reachable() && checks.values().stream().allMatch(Boolean::booleanValue);
        DistributedSmokeTestResult result = new DistributedSmokeTestResult(
                plan.planId(),
                plan.target().targetId(),
                successful,
                snapshot,
                checks,
                artifactPath(plan),
                startedAt,
                Instant.now(),
                Map.of("artifactKind", "distributed-smoke-test")
        );
        writeArtifact(result);
        return result;
    }

    private Path artifactPath(DistributedTestPlan plan) {
        return plan.artifactDirectory().resolve(plan.planId() + "-distributed-smoke-test.json");
    }

    private void writeArtifact(DistributedSmokeTestResult result) {
        try {
            Files.createDirectories(result.artifactPath().getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(result.artifactPath().toFile(), result);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to write distributed smoke test artifact: " + result.artifactPath(), ex);
        }
    }
}
