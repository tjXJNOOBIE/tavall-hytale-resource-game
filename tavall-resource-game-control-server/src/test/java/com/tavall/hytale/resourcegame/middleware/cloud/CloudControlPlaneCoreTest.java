package com.tavall.hytale.resourcegame.middleware.cloud;

import com.tavall.hytale.resourcegame.dependency.DependencyLoaderAccess;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class CloudControlPlaneCoreTest implements ICloudControlDomain {
    @Test
    void validJoinTokenRegistersNodeAndConsumesTokenWithoutStoringPlaintext() {
        InMemoryCloudRepository repository = new InMemoryCloudRepository();
        registerCloudDependencies(repository);
        Instant now = Instant.parse("2026-05-07T18:00:00Z");

        String plaintextToken = getJoinTokenCreationHandler().createJoinToken(UUID.randomUUID(), now.plus(Duration.ofHours(1)),
                Optional.of("us-west"), Set.of(CloudNodeCapability.CAN_RUN_MINECRAFT));
        NodeRegistrationResult result = getNodeRegistrationRequestHandler().register(registrationRequest(plaintextToken, "us-west"), now);

        assertTrue(result.success());
        assertTrue(result.nodeId().isPresent());
        assertTrue(repository.findIdentity(result.nodeId().orElseThrow()).isPresent());
        assertFalse(getJoinTokenValidationHandler().validate(plaintextToken, now.plusSeconds(1)).isPresent());
        assertNotEquals(plaintextToken, repository.findIdentity(result.nodeId().orElseThrow()).orElseThrow().metadata().get("tokenHash"));
    }

    @Test
    void expiredAndConsumedTokensAreRejected() {
        InMemoryCloudRepository repository = new InMemoryCloudRepository();
        registerCloudDependencies(repository);
        Instant now = Instant.parse("2026-05-07T18:00:00Z");

        String expiredToken = getJoinTokenCreationHandler().createJoinToken(UUID.randomUUID(), now.minus(Duration.ofMinutes(1)), Optional.empty(), Set.of());
        String validToken = getJoinTokenCreationHandler().createJoinToken(UUID.randomUUID(), now.plus(Duration.ofMinutes(10)), Optional.empty(), Set.of());

        assertFalse(getNodeRegistrationRequestHandler().register(registrationRequest(expiredToken, "us-west"), now).success());
        assertTrue(getNodeRegistrationRequestHandler().register(registrationRequest(validToken, "us-west"), now).success());
        assertFalse(getNodeRegistrationRequestHandler().register(registrationRequest(validToken, "us-west"), now.plusSeconds(1)).success());
    }

    @Test
    void schedulerRejectsWrongArchitectureAndMissingCapabilityThenSelectsPreferredRegion() {
        InMemoryCloudRepository repository = new InMemoryCloudRepository();
        registerCloudDependencies(repository);
        Instant now = Instant.parse("2026-05-07T18:00:00Z");
        repository.saveNode(node("arm-node", "us-west", NodeArchitecture.ARM64, Set.of(CloudNodeCapability.ARM64), CloudNodeStatus.ONLINE, now));
        CloudNode selected = node("x86-node", "us-west", NodeArchitecture.X86_64,
                Set.of(CloudNodeCapability.CAN_RUN_MINECRAFT, CloudNodeCapability.X86_64), CloudNodeStatus.ONLINE, now);
        repository.saveNode(selected);

        NodeSchedulingDecision decision = getNodeSchedulerHandler().plan(workloadRequest());

        assertTrue(decision.success());
        assertEquals(selected.nodeId(), decision.selectedNodeId().orElseThrow());
        assertEquals(1, decision.rejectedCandidates().size());
        assertTrue(decision.rejectedCandidates().getFirst().rejectionReasons().contains("architecture mismatch"));
    }

    @Test
    void reconciliationCreatesStartCommandAndAvoidsDuplicatePendingCommand() {
        InMemoryCloudRepository repository = new InMemoryCloudRepository();
        registerCloudDependencies(repository);
        Instant now = Instant.parse("2026-05-07T18:00:00Z");
        CloudNode node = node("x86-node", "us-west", NodeArchitecture.X86_64,
                Set.of(CloudNodeCapability.CAN_RUN_MINECRAFT, CloudNodeCapability.X86_64), CloudNodeStatus.ONLINE, now);
        repository.saveNode(node);
        CloudWorkload workload = getWorkloadRequestHandler().createDesiredWorkload(workloadRequest(), now);

        WorkloadReconciliationDecision first = getWorkloadReconciliationHandler().reconcile(workload.workloadId(), UUID.randomUUID(), now);
        WorkloadReconciliationDecision duplicate = getWorkloadReconciliationHandler().reconcile(workload.workloadId(), UUID.randomUUID(), now.plusSeconds(1));

        assertTrue(first.actionRequired());
        assertEquals(Optional.of(CloudCommandType.START_WORKLOAD), first.commandType());
        assertFalse(duplicate.actionRequired());
        assertEquals(1, repository.findPendingCommands(node.nodeId()).size());
    }

    @Test
    void commandResultsAreRedactedAndAgentAcceptsTypedCommandsOnly() {
        InMemoryCloudRepository repository = new InMemoryCloudRepository();
        registerCloudDependencies(repository);
        Instant now = Instant.parse("2026-05-07T18:00:00Z");
        UUID nodeId = UUID.randomUUID();
        CloudCommand command = getCloudCommandCreationHandler().create(nodeId, CloudCommandType.RUN_HEALTH_CHECK, "{}", UUID.randomUUID(), UUID.randomUUID(), now);
        CloudCommandResult result = getAgentCommandExecutionHandler().execute(command, now);
        CloudCommandResult sensitiveResult = new CloudCommandResult(command.commandId(), nodeId, true, Optional.of(0),
                "done token=abc123 password=hunter2", now, now, "ok", "", Map.of());

        assertTrue(result.success());
        assertTrue(getCloudCommandResultHandler().record(sensitiveResult));
        assertEquals(CloudCommandStatus.SUCCEEDED, repository.findCommand(command.commandId()).orElseThrow().status());
        assertEquals("done token=<redacted> password=<redacted>", repository.findCommand(command.commandId()).orElseThrow().result().orElseThrow());
    }

    @Test
    void portsRejectDuplicatesAndHeartbeatGapCreatesAlert() {
        InMemoryCloudRepository repository = new InMemoryCloudRepository();
        registerCloudDependencies(repository);
        Instant now = Instant.parse("2026-05-07T18:00:00Z");
        CloudNode node = node("x86-node", "us-west", NodeArchitecture.X86_64,
                Set.of(CloudNodeCapability.CAN_RUN_MINECRAFT, CloudNodeCapability.X86_64), CloudNodeStatus.ONLINE,
                now.minus(Duration.ofMinutes(10)));
        repository.saveNode(node);
        UUID workloadId = UUID.randomUUID();

        getPortAllocationHandler().allocate(workloadId, node.nodeId(), PortProtocol.TCP, 25568, 25568, now);

        assertThrows(IllegalStateException.class, () -> getPortAllocationHandler().allocate(UUID.randomUUID(), node.nodeId(), PortProtocol.TCP, 25568, 25568, now));
        assertTrue(getAlertEvaluationHandler().evaluateHeartbeat(node, now, Duration.ofMinutes(5)).isPresent());
        assertEquals(CloudNodeStatus.OFFLINE, repository.findNode(node.nodeId()).orElseThrow().nodeStatus());
    }

    private void registerCloudDependencies(InMemoryCloudRepository repository) {
        DependencyLoaderAccess.clear();
        DependencyLoaderAccess.registerInstance(CloudRepository.class, repository);
        new CloudControlDependencyModule().registerDependencies();
    }

    private NodeRegistrationRequest registrationRequest(String token, String region) {
        return new NodeRegistrationRequest(token, "oracle-arm-1", "203.0.113.10", "10.0.0.10", region, "home-lab",
                "oracle", "linux", NodeArchitecture.ARM64, "0.1.0", 4, 8192, 6144, 120, 90,
                Set.of(CloudNodeCapability.CAN_RUN_MINECRAFT, CloudNodeCapability.ARM64), Set.of(SupportedRuntime.SYSTEMD),
                Set.of("kingdom"), Map.of("cpuModel", "ampere"));
    }

    private CloudNode node(String hostname, String region, NodeArchitecture architecture, Set<CloudNodeCapability> capabilities, CloudNodeStatus status, Instant now) {
        return new CloudNode(UUID.randomUUID(), hostname, "203.0.113.20", "10.0.0.20", region, "dc-1", "owned",
                "test-cpu", 8, 16384, 12000, 250, 200, "linux", architecture, status, capabilities, now, now,
                Set.of(), Map.of());
    }

    private WorkloadRequest workloadRequest() {
        return new WorkloadRequest(CloudWorkloadType.MINECRAFT_SERVER, "mc-kingdom-server-1", Optional.of("us-west"),
                Set.of(CloudNodeCapability.CAN_RUN_MINECRAFT, CloudNodeCapability.X86_64), Optional.of(NodeArchitecture.X86_64),
                new ResourceLimits(2, 2048, 20, Map.of()), Map.of(), Set.of(), null, Map.of("version", "1.21.4"));
    }
}
