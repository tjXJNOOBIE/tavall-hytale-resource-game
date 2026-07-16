package org.tavall.control.web;

import org.tavall.control.ControlServerDependencyModule;
import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import org.tavall.control.cloud.CloudAgentHeartbeatPayload;
import org.tavall.control.cloud.CloudCommand;
import org.tavall.control.cloud.CloudCommandResult;
import org.tavall.control.cloud.CloudCommandStatus;
import org.tavall.control.cloud.CloudCommandType;
import org.tavall.control.cloud.CloudNode;
import org.tavall.control.cloud.CloudNodeCapability;
import org.tavall.control.cloud.CloudNodeStatus;
import org.tavall.control.cloud.CloudRepository;
import org.tavall.control.cloud.NodeArchitecture;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CloudAgentIngressControllerTest {
    @Test
    void agentEndpointsUsePlainJavaCloudHandlers() {
        new ControlServerDependencyModule().registerDependencies();
        CloudRepository repository = DependencyLoaderAccess.findInstance(CloudRepository.class);
        UUID nodeId = UUID.randomUUID();
        UUID commandId = UUID.randomUUID();
        Instant now = Instant.now();
        repository.saveNode(testNode(nodeId, now));
        repository.saveCommand(new CloudCommand(
                commandId,
                nodeId,
                CloudCommandType.RUN_HEALTH_CHECK,
                "{\"workloadId\":\"" + UUID.randomUUID() + "\"}",
                UUID.randomUUID(),
                now,
                CloudCommandStatus.QUEUED,
                Optional.empty(),
                UUID.randomUUID(),
                Optional.empty(),
                Optional.empty(),
                Map.of()
        ));
        CloudAgentIngressController controller = new CloudAgentIngressController();

        ResponseEntity<Map<String, Object>> heartbeat = controller.heartbeat(
                nodeId,
                new CloudAgentHeartbeatPayload(nodeId, UUID.randomUUID(), "test-agent", now.plusSeconds(5), Map.of())
        );
        ResponseEntity<List<CloudCommand>> commands = controller.commands(nodeId, 10);
        ResponseEntity<Map<String, Object>> result = controller.result(
                nodeId,
                new CloudCommandResult(
                        commandId,
                        nodeId,
                        true,
                        Optional.of(0),
                        "health token=secret-value",
                        now,
                        now.plusSeconds(1),
                        "ok",
                        "",
                        Map.of()
                )
        );

        assertEquals(200, heartbeat.getStatusCode().value());
        assertEquals(1, commands.getBody().size());
        assertEquals(CloudCommandStatus.SENT, commands.getBody().getFirst().status());
        assertEquals(200, result.getStatusCode().value());
        assertEquals(CloudNodeStatus.ONLINE, repository.findNode(nodeId).orElseThrow().nodeStatus());
        assertTrue(repository.findCommand(commandId).orElseThrow().result().orElseThrow().contains("token=<redacted>"));
    }

    private CloudNode testNode(UUID nodeId, Instant now) {
        return new CloudNode(
                nodeId,
                "node-test",
                "127.0.0.1",
                "127.0.0.1",
                "local",
                "dev",
                "local",
                "test-cpu",
                4,
                8192,
                4096,
                128,
                64,
                "linux",
                NodeArchitecture.X86_64,
                CloudNodeStatus.REGISTERING,
                Set.of(CloudNodeCapability.CAN_RUN_MINECRAFT),
                now,
                now,
                Set.of("test"),
                Map.of()
        );
    }
}
