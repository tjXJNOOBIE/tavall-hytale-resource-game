package org.tavall.control.distribution.remote;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.tavall.control.distribution.IDistributionDomain;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DistributedTestRunnerHandler implements IDistributedTestRunnerHandler, IDistributionDomain {
    public DistributedSmokeTestResult runDistributedSmokeTest(DistributedTestPlan plan) {
        Instant startedAt = Instant.now();
        RemoteEnvironmentSnapshot snapshot = plan.target().localTarget()
                ? getRemoteEnvironmentProbeHandler().detectLocalEnvironment()
                : getRemoteEnvironmentProbeHandler().probeRemoteEnvironment(plan.target());
        Map<String, Boolean> checks = new LinkedHashMap<>();
        if (plan.verifyRedis()) {
            checks.put("redis", getRemoteHealthCheckHandler().verifyRedisConnectivity(plan.target()));
        }
        if (plan.verifyPostgres()) {
            checks.put("postgres", getRemoteHealthCheckHandler().verifyPostgresConnectivity(plan.target()));
        }
        if (plan.verifyHytale()) {
            checks.put("hytale", getRemoteHealthCheckHandler().verifyHytaleServerReachable(plan.target()));
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
            new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .registerModule(new Jdk8Module())
                    .writerWithDefaultPrettyPrinter()
                    .writeValue(result.artifactPath().toFile(), result);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to write distributed smoke test artifact: " + result.artifactPath(), ex);
        }
    }
}
