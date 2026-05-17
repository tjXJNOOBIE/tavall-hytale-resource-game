package com.tavall.resourcegame.middleware.cloud;

import com.tavall.resourcegame.dependency.DependencyLoaderAccess;
import com.tavall.resourcegame.middleware.authority.AuthorityRepository;
import com.tavall.resourcegame.middleware.authority.AuthorityScope;
import com.tavall.resourcegame.middleware.authority.ControlAuthority;
import com.tavall.resourcegame.middleware.authority.ControlAuthorityLevel;
import com.tavall.resourcegame.middleware.control.ControlPermission;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        NodeRegistrationResult result = getNodeRegistrationRequestHandler().register(registrationRequest(plaintextToken), now);

        assertTrue(result.success());
        assertTrue(result.nodeId().isPresent());
        assertTrue(repository.findIdentity(result.nodeId().orElseThrow()).isPresent());
        assertFalse(getJoinTokenValidationHandler().validate(plaintextToken, now.plusSeconds(1)).isPresent());
    }

    @Test
    void schedulerRejectsWrongArchitectureAndSelectsPreferredRegion() {
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
        assertTrue(decision.rejectedCandidates().getFirst().rejectionReasons().contains("architecture mismatch"));
    }

    @Test
    void reconciliationCreatesRuntimeAwareCommandAndAvoidsDuplicatePendingCommand() {
        InMemoryCloudRepository repository = new InMemoryCloudRepository();
        registerCloudDependencies(repository);
        Instant now = Instant.parse("2026-05-07T18:00:00Z");
        CloudNode node = node("x86-node", "us-west", NodeArchitecture.X86_64,
                Set.of(CloudNodeCapability.CAN_RUN_MINECRAFT, CloudNodeCapability.X86_64), CloudNodeStatus.ONLINE, now);
        repository.saveNode(node);
        CloudWorkload workload = getWorkloadRequestHandler().createDesiredWorkload(workloadRequest(), now);
        UUID requestedBy = grantGlobalCloudOperator(now);

        WorkloadReconciliationDecision first = getWorkloadReconciliationHandler().reconcile(workload.workloadId(), requestedBy, now);
        WorkloadReconciliationDecision duplicate = getWorkloadReconciliationHandler().reconcile(workload.workloadId(), requestedBy, now.plusSeconds(1));

        assertTrue(first.actionRequired());
        assertEquals(Optional.of(CloudCommandType.START_WORKLOAD), first.commandType());
        assertFalse(duplicate.actionRequired());
        assertEquals(1, repository.findPendingCommands(node.nodeId()).size());
        assertTrue(repository.findPendingCommands(node.nodeId()).getFirst().payloadJson().contains("\"runtime\":\"tmux\""));
    }

    @Test
    void commandResultsAreRedactedAndAgentPollMarksCommandSent() {
        InMemoryCloudRepository repository = new InMemoryCloudRepository();
        registerCloudDependencies(repository);
        Instant now = Instant.parse("2026-05-07T18:00:00Z");
        UUID nodeId = UUID.randomUUID();
        UUID requestedBy = grantGlobalCloudOperator(now);
        CloudCommand command = getCloudCommandCreationHandler().create(nodeId, CloudCommandType.RUN_HEALTH_CHECK, "{}",
                requestedBy, UUID.randomUUID(), now);

        assertEquals(1, getAgentCommandPollHandler().poll(nodeId, 10, now).size());
        assertEquals(CloudCommandStatus.SENT, repository.findCommand(command.commandId()).orElseThrow().status());

        CloudCommandResult sensitiveResult = new CloudCommandResult(command.commandId(), nodeId, true, Optional.of(0),
                "done authorization=Bearer abc123 apiKey=abc123 secret=abc123", now, now, "ok", "", Map.of());

        assertTrue(getCloudCommandResultHandler().record(sensitiveResult));
        assertEquals("done authorization=<redacted> apiKey=<redacted> secret=<redacted>",
                repository.findCommand(command.commandId()).orElseThrow().result().orElseThrow());
    }

    @Test
    void cloudCliCreatesTokensInspectsResourcesAndUpdatesDesiredState() {
        InMemoryCloudRepository repository = new InMemoryCloudRepository();
        registerCloudDependencies(repository);
        Instant now = Instant.parse("2026-05-07T18:00:00Z");
        CloudNode node = node("x86-node", "us-west", NodeArchitecture.X86_64,
                Set.of(CloudNodeCapability.CAN_RUN_MINECRAFT, CloudNodeCapability.X86_64), CloudNodeStatus.ONLINE, now);
        repository.saveNode(node);
        CloudWorkload workload = getWorkloadRequestHandler().createDesiredWorkload(workloadRequest(), now);
        UUID requestedBy = grantGlobalCloudOperator(now);

        String tokenOutput = getCloudControlCliHandler().execute("cloud nodes create-token 60 us-west", requestedBy, now);
        String token = tokenOutput.substring("joinToken=".length()).trim();

        assertTrue(getJoinTokenValidationHandler().validate(token, now.plusSeconds(1)).isPresent());
        assertTrue(getCloudControlCliHandler().execute("cloud nodes inspect " + node.nodeId(), requestedBy, now).contains("hostname=x86-node"));
        String workloadInspect = getCloudControlCliHandler().execute("cloud workloads inspect " + workload.workloadId(), requestedBy, now);
        assertTrue(workloadInspect.contains("desired=RUNNING"));
        assertTrue(workloadInspect.contains("session=mc-kingdom-server-1"));
        assertEquals("Created START_WORKLOAD command." + System.lineSeparator(),
                getCloudControlCliHandler().execute("cloud workloads start " + workload.workloadId(), requestedBy, now));
        assertEquals("commands=1" + System.lineSeparator(), getCloudControlCliHandler().execute("cloud commands list", requestedBy, now));
    }

    @Test
    void cloudCliCanBootstrapNodeFromJoinTokenAndThenCreateWorkloads() {
        InMemoryCloudRepository repository = new InMemoryCloudRepository();
        registerCloudDependencies(repository);
        Instant now = Instant.parse("2026-05-07T18:00:00Z");
        UUID requestedBy = grantGlobalCloudOperator(now);

        String tokenOutput = getCloudControlCliHandler().execute("cloud nodes create-token 60", requestedBy, now);
        String token = tokenOutput.substring("joinToken=".length()).trim();
        String registerOutput = getCloudControlCliHandler().execute("cloud nodes register " + token + " bootstrap-node us-west", requestedBy, now);

        assertTrue(registerOutput.contains("success=true"));
        assertTrue(getCloudControlCliHandler().execute("cloud nodes list", requestedBy, now).contains("nodes=1"));

        String workloadOutput = getCloudControlCliHandler().execute("cloud workloads create BOT_TEST_WORKER smoke-worker", requestedBy, now);

        assertTrue(workloadOutput.contains("workload="));
        assertTrue(workloadOutput.contains("node="));
        assertTrue(workloadOutput.contains("session=smoke-worker"));
        assertTrue(getCloudControlCliHandler().execute("cloud servers list", requestedBy, now).contains("servers=1"));
        assertEquals(1, repository.findNodes().size());
        assertEquals(1, repository.findWorkloads().size());
    }

    @Test
    void cloudCliRunsSchedulerWorkloadPortBackupCommandAndAlertOperations() {
        InMemoryCloudRepository repository = new InMemoryCloudRepository();
        registerCloudDependencies(repository);
        Instant now = Instant.parse("2026-05-07T18:00:00Z");
        CloudNode node = node("x86-node", "us-west", NodeArchitecture.X86_64,
                Set.of(CloudNodeCapability.CAN_RUN_MINECRAFT, CloudNodeCapability.X86_64), CloudNodeStatus.ONLINE, now);
        repository.saveNode(node);
        UUID requestedBy = grantGlobalCloudOperator(now);

        assertTrue(getCloudControlCliHandler().execute("cloud scheduler plan MINECRAFT_SERVER mc-kingdom-server-1 us-west", requestedBy, now)
                .contains("success=true"));
        assertTrue(getCloudControlCliHandler().execute("cloud scheduler candidates MINECRAFT_SERVER mc-kingdom-server-1 us-west", requestedBy, now)
                .contains("eligible=1"));
        String workloadOutput = getCloudControlCliHandler()
                .execute("cloud workloads create MINECRAFT_SERVER mc-kingdom-server-1 us-west", requestedBy, now);
        UUID workloadId = UUID.fromString(workloadOutput.substring("workload=".length(), workloadOutput.indexOf(" node=")));

        assertTrue(getCloudControlCliHandler().execute("cloud ports allocate " + workloadId + " 25575", requestedBy, now)
                .contains("public=25575/TCP"));
        assertTrue(getCloudControlCliHandler().execute("cloud backups run " + workloadId + " /srv/mc world://backup", requestedBy, now)
                .startsWith("backupJob="));
        CloudCommand backupCommand = repository.findCommands().stream()
                .filter(command -> command.commandType() == CloudCommandType.RUN_BACKUP)
                .findFirst()
                .orElseThrow();
        assertTrue(getCloudControlCliHandler().execute("cloud commands retry " + backupCommand.commandId(), requestedBy, now.plusSeconds(1))
                .contains("retryOf=" + backupCommand.commandId()));
        assertEquals("command=" + backupCommand.commandId() + " status=CANCELLED" + System.lineSeparator(),
                getCloudControlCliHandler().execute("cloud commands cancel " + backupCommand.commandId(), requestedBy, now.plusSeconds(2)));

        CloudAlert alert = new CloudAlert(UUID.randomUUID(), CloudAlertType.BACKUP_FAILED, CloudAlertSeverity.WARNING,
                "workload", workloadId.toString(), "backup failed", CloudAlertState.ACTIVE, now, Optional.empty(), Map.of());
        repository.saveAlert(alert);

        assertEquals("alert=" + alert.alertId() + " state=ACKNOWLEDGED" + System.lineSeparator(),
                getCloudControlCliHandler().execute("cloud alerts ack " + alert.alertId(), requestedBy, now.plusSeconds(3)));
    }

    @Test
    void metricIngestionStoresSnapshotsAndCreatesCapacityAndCrashLoopAlerts() {
        InMemoryCloudRepository repository = new InMemoryCloudRepository();
        registerCloudDependencies(repository);
        Instant now = Instant.parse("2026-05-07T18:00:00Z");
        UUID nodeId = UUID.randomUUID();
        UUID workloadId = UUID.randomUUID();

        getNodeMetricIngestionHandler().ingest(new NodeMetricSnapshot(nodeId, 44.5D, 9500, 500, 95, 5,
                1024L, 2048L, now, Map.of("source", "agent")));
        getWorkloadMetricIngestionHandler().ingest(new WorkloadMetricSnapshot(workloadId, nodeId, Optional.of(72.5D),
                Optional.of(1400), 3, WorkloadHealthStatus.UNHEALTHY, now.plusSeconds(1), Map.of("runtime", "systemd")));

        assertEquals(1, repository.findNodeMetrics(nodeId).size());
        assertEquals(1, repository.findWorkloadMetrics(workloadId).size());
        assertTrue(repository.findAlerts().stream().anyMatch(alert -> alert.alertType() == CloudAlertType.NODE_RAM_HIGH));
        assertTrue(repository.findAlerts().stream().anyMatch(alert -> alert.alertType() == CloudAlertType.NODE_DISK_HIGH));
        assertTrue(repository.findAlerts().stream().anyMatch(alert -> alert.alertType() == CloudAlertType.WORKLOAD_CRASH_LOOP));
    }

    @Test
    void backupResultVerificationAndRestoreFlowUseTypedHighRiskCommand() {
        InMemoryCloudRepository repository = new InMemoryCloudRepository();
        registerCloudDependencies(repository);
        Instant now = Instant.parse("2026-05-07T18:00:00Z");
        UUID workloadId = UUID.randomUUID();
        UUID nodeId = UUID.randomUUID();
        UUID requestedBy = grantGlobalCloudOperator(now);
        BackupPlan plan = new BackupPlan(UUID.randomUUID(), Optional.of(workloadId), Optional.of(nodeId),
                BackupType.WORLD_FOLDER, "manual", true, "7d", "s3://backups/worlds", true, Map.of());
        BackupJob job = getBackupJobCreationHandler().createAndCommand(plan, "/srv/worlds", requestedBy, now);
        CloudCommand backupCommand = repository.findCommands().getFirst();

        assertTrue(getBackupResultHandler().record(new CloudCommandResult(backupCommand.commandId(), nodeId, true,
                Optional.of(0), "backup complete", now, now.plusSeconds(3), "ok", "", Map.of("checksum", "sha256:abc"))));
        assertTrue(getBackupVerificationHandler().verify(job.backupId()));
        assertEquals(BackupVerificationStatus.VERIFIED, repository.findBackupJob(job.backupId()).orElseThrow().verificationStatus());

        assertThrows(CloudCommandAuthorizationException.class, () -> getRestoreJobCreationHandler()
                .createAndCommand(job.backupId(), Optional.of(workloadId), Optional.of(nodeId), requestedBy, false, now.plusSeconds(4)));

        RestoreJob restoreJob = getRestoreJobCreationHandler()
                .createAndCommand(job.backupId(), Optional.of(workloadId), Optional.of(nodeId), requestedBy, true, now.plusSeconds(5));
        CloudCommand restoreCommand = repository.findCommands().stream()
                .filter(command -> command.commandType() == CloudCommandType.RESTORE_BACKUP)
                .findFirst()
                .orElseThrow();

        assertEquals(RestoreStatus.PENDING, restoreJob.status());
        assertTrue(getRestoreResultHandler().record(new CloudCommandResult(restoreCommand.commandId(), nodeId, true,
                Optional.of(0), "restore complete", now.plusSeconds(6), now.plusSeconds(9), "ok", "", Map.of())));
        assertEquals(RestoreStatus.COMPLETED, repository.findRestoreJob(restoreJob.restoreId()).orElseThrow().status());
    }

    @Test
    void cloudCliListsWorkloadsServersConsolesAndKingdomDirectories() {
        InMemoryCloudRepository repository = new InMemoryCloudRepository();
        registerCloudDependencies(repository);
        Instant now = Instant.parse("2026-05-07T18:00:00Z");
        CloudNode node = node("x86-node", "us-west", NodeArchitecture.X86_64,
                Set.of(CloudNodeCapability.CAN_RUN_MINECRAFT, CloudNodeCapability.X86_64), CloudNodeStatus.ONLINE, now);
        repository.saveNode(node);
        CloudWorkload workload = getWorkloadRequestHandler().createDesiredWorkload(workloadRequest(), now);
        UUID requestedBy = grantGlobalCloudOperator(now);

        String workloads = getCloudControlCliHandler().execute("cloud workloads list", requestedBy, now);
        String servers = getCloudControlCliHandler().execute("cloud servers list", requestedBy, now);
        String consoles = getCloudControlCliHandler().execute("cloud consoles list", requestedBy, now);
        String kingdoms = getCloudControlCliHandler().execute("cloud kingdoms list", requestedBy, now);
        String attach = getCloudControlCliHandler().execute("cloud consoles attach " + workload.workloadId(), requestedBy, now);
        String ensure = getCloudControlCliHandler().execute("cloud kingdoms ensure kingdom-2", requestedBy, now);

        assertTrue(workloads.contains("workloads=1"));
        assertTrue(workloads.contains("session=" + getCloudControlPlaneFilesystemLayout().workloadSessionName(workload)));
        assertTrue(workloads.contains("attach=tmux attach -t "));
        assertTrue(servers.contains("servers=1"));
        assertTrue(consoles.contains("consoles=2"));
        assertTrue(consoles.contains("console=" + getCloudControlPlaneFilesystemLayout().controlPlaneSessionName()));
        assertTrue(kingdoms.contains("kingdoms=1"));
        assertTrue(attach.contains("attach=tmux attach -t "));
        assertTrue(ensure.contains("kingdom=kingdom-2"));
        assertTrue(Files.exists(getCloudControlPlaneFilesystemLayout().kingdomDir("kingdom-2")));
    }

    private void registerCloudDependencies(InMemoryCloudRepository repository) {
        DependencyLoaderAccess.clear();
        DependencyLoaderAccess.registerInstance(CloudRepository.class, repository);
        DependencyLoaderAccess.registerInstance(CloudControlPlaneFilesystemLayout.class,
                new CloudControlPlaneFilesystemLayout(createTempRoot()));
        new CloudControlDependencyModule().registerDependencies();
        getCloudControlPlaneFilesystemLayout().ensureLayout();
    }

    private Path createTempRoot() {
        try {
            return Files.createTempDirectory("cloud-control-plane-test");
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to create cloud control plane test root", exception);
        }
    }

    private UUID grantGlobalCloudOperator(Instant now) {
        UUID principalId = UUID.randomUUID();
        DependencyLoaderAccess.findInstance(AuthorityRepository.class).saveAuthority(ControlAuthority.enabled(
                principalId,
                ControlAuthorityLevel.C5_GLOBAL_AUTHORITY,
                AuthorityScope.global(),
                EnumSet.allOf(ControlPermission.class),
                principalId,
                now.toEpochMilli(),
                true,
                true
        ));
        return principalId;
    }

    private NodeRegistrationRequest registrationRequest(String token) {
        return new NodeRegistrationRequest(token, "oracle-arm-1", "203.0.113.10", "10.0.0.10", "us-west", "home-lab",
                "oracle", "linux", NodeArchitecture.ARM64, "0.1.0", 4, 8192, 6144, 120, 90,
                Set.of(CloudNodeCapability.CAN_RUN_MINECRAFT, CloudNodeCapability.ARM64),
                Set.of(SupportedRuntime.SYSTEMD), Set.of("kingdom"), Map.of());
    }

    private CloudNode node(String hostname, String region, NodeArchitecture architecture,
                           Set<CloudNodeCapability> capabilities, CloudNodeStatus status, Instant now) {
        return new CloudNode(UUID.randomUUID(), hostname, "203.0.113.20", "10.0.0.20", region, "dc-1", "owned",
                "test-cpu", 8, 16384, 12000, 250, 200, "linux", architecture, status, capabilities,
                now, now, Set.of(), Map.of());
    }

    private WorkloadRequest workloadRequest() {
        return new WorkloadRequest(CloudWorkloadType.MINECRAFT_SERVER, "mc-kingdom-server-1", Optional.of("us-west"),
                Set.of(CloudNodeCapability.CAN_RUN_MINECRAFT, CloudNodeCapability.X86_64), Optional.of(NodeArchitecture.X86_64),
                new ResourceLimits(2, 2048, 10, Map.of()), Map.of(), Set.of(),
                new SchedulingPolicy(Optional.of("us-west"), Set.of(CloudNodeCapability.CAN_RUN_MINECRAFT, CloudNodeCapability.X86_64),
                        Optional.of(NodeArchitecture.X86_64), true, true, true, Map.of()),
                Map.of("runtime", "tmux"));
    }
}
