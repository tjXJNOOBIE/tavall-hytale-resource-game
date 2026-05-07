package com.tavall.hytale.resourcegame.distribution;

import com.tavall.hytale.resourcegame.distribution.node.DistributedNode;
import com.tavall.hytale.resourcegame.distribution.node.DistributedNodeStatus;
import com.tavall.hytale.resourcegame.distribution.node.DistributedNodeType;
import com.tavall.hytale.resourcegame.distribution.node.NodeCapability;
import com.tavall.hytale.resourcegame.distribution.node.NodeRegistryHandler;
import com.tavall.hytale.resourcegame.distribution.remote.DistributedSmokeTestResult;
import com.tavall.hytale.resourcegame.distribution.remote.DistributedTestPlan;
import com.tavall.hytale.resourcegame.distribution.remote.DistributedTestRunnerHandler;
import com.tavall.hytale.resourcegame.distribution.remote.RemoteCommand;
import com.tavall.hytale.resourcegame.distribution.remote.RemoteCommandHandler;
import com.tavall.hytale.resourcegame.distribution.remote.RemoteCommandPolicy;
import com.tavall.hytale.resourcegame.distribution.remote.RemoteCommandResult;
import com.tavall.hytale.resourcegame.distribution.remote.RemoteEnvironmentProbeHandler;
import com.tavall.hytale.resourcegame.distribution.remote.RemoteTarget;
import com.tavall.hytale.resourcegame.distribution.health.RemoteHealthCheckHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class DistributedRuntimeHandlerTest {
    @TempDir
    Path tempDir;

    @Test
    void nodeRegistryTracksRegistrationHeartbeatCapabilitiesAndOfflineState() {
        Instant startedAt = Instant.parse("2026-05-07T12:00:00Z");
        NodeRegistryHandler registryHandler = new NodeRegistryHandler();
        DistributedNode node = new DistributedNode(
                "local-dev-1",
                DistributedNodeType.LOCAL_DEV,
                "local-host",
                "local",
                "127.0.0.1",
                "127.0.0.1",
                42L,
                startedAt,
                startedAt,
                Set.of(NodeCapability.CAN_DISPATCH_EVENTS, NodeCapability.CAN_RUN_ADMIN_UI),
                DistributedNodeStatus.STARTING,
                Map.of()
        );

        registryHandler.registerNode(node);
        DistributedNode heartbeat = registryHandler.heartbeat("local-dev-1", startedAt.plusSeconds(5));

        assertEquals(DistributedNodeStatus.ONLINE, heartbeat.status());
        assertEquals(1, registryHandler.getOnlineNodes().size());
        assertEquals(1, registryHandler.getNodesByCapability(NodeCapability.CAN_DISPATCH_EVENTS).size());

        int offlineCount = registryHandler.markMissingHeartbeatsOffline(Duration.ofSeconds(30), startedAt.plusSeconds(45));

        assertEquals(1, offlineCount);
        assertEquals(DistributedNodeStatus.OFFLINE, registryHandler.findNode("local-dev-1").orElseThrow().status());
    }

    @Test
    void remoteCommandPolicyRejectsCredentialShapedCommandsBeforeExecution() {
        RemoteCommandHandler commandHandler = new RemoteCommandHandler(RemoteCommandPolicy.defaults());
        RemoteCommand command = new RemoteCommand(
                "unsafe",
                "ssh",
                List.of("cat", "C:/Users/TJ/.ssh/id_rsa"),
                Duration.ofSeconds(1),
                false,
                Map.of()
        );

        RemoteCommandResult result = commandHandler.runRemoteCommand(RemoteTarget.local(), command);

        assertFalse(result.successful());
        assertEquals(-1, result.exitCode());
        assertTrue(result.stderr().contains("credential"));
    }

    @Test
    void localDistributedSmokeTestWritesCompactArtifact() throws Exception {
        RemoteCommandHandler commandHandler = new RemoteCommandHandler(RemoteCommandPolicy.defaults());
        DistributedTestRunnerHandler runnerHandler = new DistributedTestRunnerHandler(
                new RemoteEnvironmentProbeHandler(commandHandler),
                new RemoteHealthCheckHandler()
        );
        DistributedTestPlan plan = new DistributedTestPlan(
                "local-smoke",
                RemoteTarget.local(),
                tempDir,
                false,
                false,
                false,
                Map.of()
        );

        DistributedSmokeTestResult result = runnerHandler.runDistributedSmokeTest(plan);

        assertTrue(result.successful());
        assertTrue(Files.exists(result.artifactPath()));
        assertEquals("local", result.environmentSnapshot().targetId());
    }
}
