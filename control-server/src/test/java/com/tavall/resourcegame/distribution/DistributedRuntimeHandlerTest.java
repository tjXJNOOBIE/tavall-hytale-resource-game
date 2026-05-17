package com.tavall.resourcegame.distribution;

import com.tavall.resourcegame.distribution.node.DistributedNode;
import com.tavall.resourcegame.distribution.node.DistributedNodeStatus;
import com.tavall.resourcegame.distribution.node.DistributedNodeType;
import com.tavall.resourcegame.distribution.node.NodeCapability;
import com.tavall.resourcegame.distribution.node.NodeRegistryHandler;
import com.tavall.resourcegame.distribution.remote.DistributedSmokeTestResult;
import com.tavall.resourcegame.distribution.remote.DistributedTestPlan;
import com.tavall.resourcegame.distribution.remote.RemoteCommand;
import com.tavall.resourcegame.distribution.remote.RemoteCommandResult;
import com.tavall.resourcegame.distribution.remote.RemoteTarget;
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

public final class DistributedRuntimeHandlerTest implements IDistributionDomain {
    @TempDir
    Path tempDir;

    @Test
    void nodeRegistryTracksRegistrationHeartbeatCapabilitiesAndOfflineState() {
        new DistributionDependencyModule().registerDependencies();
        Instant startedAt = Instant.parse("2026-05-07T12:00:00Z");
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

        getNodeRegistryHandler().registerNode(node);
        DistributedNode heartbeat = getNodeRegistryHandler().heartbeat("local-dev-1", startedAt.plusSeconds(5));

        assertEquals(DistributedNodeStatus.ONLINE, heartbeat.status());
        assertEquals(1, getNodeRegistryHandler().getOnlineNodes().size());
        assertEquals(1, getNodeRegistryHandler().getNodesByCapability(NodeCapability.CAN_DISPATCH_EVENTS).size());

        int offlineCount = getNodeRegistryHandler().markMissingHeartbeatsOffline(Duration.ofSeconds(30), startedAt.plusSeconds(45));

        assertEquals(1, offlineCount);
        assertEquals(DistributedNodeStatus.OFFLINE, getNodeRegistryHandler().findNode("local-dev-1").orElseThrow().status());
    }

    @Test
    void remoteCommandPolicyRejectsCredentialShapedCommandsBeforeExecution() {
        new DistributionDependencyModule().registerDependencies();
        RemoteCommand command = new RemoteCommand(
                "unsafe",
                "ssh",
                List.of("cat", "C:/Users/TJ/.ssh/id_rsa"),
                Duration.ofSeconds(1),
                false,
                Map.of()
        );

        RemoteCommandResult result = getRemoteCommandHandler().runRemoteCommand(RemoteTarget.local(), command);

        assertFalse(result.successful());
        assertEquals(-1, result.exitCode());
        assertTrue(result.stderr().contains("credential"));
    }

    @Test
    void localDistributedSmokeTestWritesCompactArtifact() throws Exception {
        new DistributionDependencyModule().registerDependencies();
        DistributedTestPlan plan = new DistributedTestPlan(
                "local-smoke",
                RemoteTarget.local(),
                tempDir,
                false,
                false,
                false,
                Map.of()
        );

        DistributedSmokeTestResult result = getDistributedTestRunnerHandler().runDistributedSmokeTest(plan);

        assertTrue(result.successful());
        assertTrue(Files.exists(result.artifactPath()));
        assertEquals("local", result.environmentSnapshot().targetId());
    }
}
