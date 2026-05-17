package com.tavall.resourcegame.middleware.cloud;

import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class CloudAgentRuntimeTest implements ICloudAgentDomain {
    @Test
    void runtimeHeartbeatsPollsCommandsExecutesAndReportsResults() throws Exception {
        DependencyLoaderAccess.clear();
        UUID nodeId = UUID.fromString("00000000-0000-0000-0000-000000000123");
        RecordingCloudAgentTransportHandler transport = new RecordingCloudAgentTransportHandler();
        transport.addCommand(new CloudCommand(
                UUID.randomUUID(),
                nodeId,
                CloudCommandType.RUN_HEALTH_CHECK,
                "{}",
                UUID.randomUUID(),
                Instant.parse("2026-05-08T18:00:00Z"),
                CloudCommandStatus.QUEUED,
                Optional.empty(),
                UUID.randomUUID(),
                Optional.empty(),
                Optional.empty(),
                Map.of()
        ));
        DependencyLoaderAccess.registerInstance(CloudAgentRuntimeConfig.class, new CloudAgentRuntimeConfig(
                URI.create("http://127.0.0.1:18080"),
                nodeId,
                UUID.fromString("00000000-0000-0000-0000-000000000456"),
                "test-agent",
                50L
        ));
        DependencyLoaderAccess.registerInstance(ICloudAgentTransportHandler.class, transport);
        new CloudAgentDependencyModule().registerDependencies();

        CloudAgentRuntimeCycleResult result = getCloudAgentRuntime().runOnce(Instant.parse("2026-05-08T18:00:01Z"));

        assertTrue(result.heartbeatAccepted());
        assertEquals(1, result.commandsReceived());
        assertEquals(1, result.commandsExecuted());
        assertEquals(1, result.resultsReported());
        assertEquals(1, transport.heartbeatCount());
        assertEquals(1, transport.results().size());
        assertTrue(transport.results().getFirst().success());
    }
}
