package com.tavall.hytale.resourcegame.middleware.cloud;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryCloudRepository implements CloudRepository {
    private final Map<UUID, JoinToken> joinTokens = new ConcurrentHashMap<>();
    private final Map<String, UUID> joinTokenIdsByHash = new ConcurrentHashMap<>();
    private final Map<UUID, CloudNode> nodes = new ConcurrentHashMap<>();
    private final Map<UUID, NodeIdentity> identities = new ConcurrentHashMap<>();
    private final Map<UUID, NodeAgentRuntime> agents = new ConcurrentHashMap<>();
    private final Map<UUID, CloudWorkload> workloads = new ConcurrentHashMap<>();
    private final Map<UUID, CloudCommand> commands = new ConcurrentHashMap<>();
    private final Map<UUID, PortAllocation> ports = new ConcurrentHashMap<>();
    private final Map<UUID, BackupPlan> backupPlans = new ConcurrentHashMap<>();
    private final Map<UUID, BackupJob> backupJobs = new ConcurrentHashMap<>();
    private final Map<UUID, CloudAlert> alerts = new ConcurrentHashMap<>();

    public void saveJoinToken(JoinToken joinToken) {
        joinTokens.put(joinToken.joinTokenId(), joinToken);
        joinTokenIdsByHash.put(joinToken.tokenHash(), joinToken.joinTokenId());
    }

    public Optional<JoinToken> findJoinTokenByHash(String tokenHash) {
        UUID id = joinTokenIdsByHash.get(tokenHash);
        return id == null ? Optional.empty() : Optional.ofNullable(joinTokens.get(id));
    }

    public void saveNode(CloudNode node) {
        nodes.put(node.nodeId(), node);
    }

    public Optional<CloudNode> findNode(UUID nodeId) {
        return Optional.ofNullable(nodes.get(nodeId));
    }

    public List<CloudNode> findNodes() {
        return nodes.values().stream().sorted(Comparator.comparing(CloudNode::hostname)).toList();
    }

    public void saveIdentity(NodeIdentity identity) {
        identities.put(identity.nodeId(), identity);
    }

    public Optional<NodeIdentity> findIdentity(UUID nodeId) {
        return Optional.ofNullable(identities.get(nodeId));
    }

    public void saveAgent(NodeAgentRuntime agent) {
        agents.put(agent.agentId(), agent);
    }

    public void saveWorkload(CloudWorkload workload) {
        workloads.put(workload.workloadId(), workload);
    }

    public Optional<CloudWorkload> findWorkload(UUID workloadId) {
        return Optional.ofNullable(workloads.get(workloadId));
    }

    public List<CloudWorkload> findWorkloads() {
        return new ArrayList<>(workloads.values());
    }

    public void saveCommand(CloudCommand command) {
        commands.put(command.commandId(), command);
    }

    public Optional<CloudCommand> findCommand(UUID commandId) {
        return Optional.ofNullable(commands.get(commandId));
    }

    public List<CloudCommand> findPendingCommands(UUID nodeId) {
        return commands.values().stream()
                .filter(command -> command.nodeId().equals(nodeId))
                .filter(command -> command.status() == CloudCommandStatus.CREATED || command.status() == CloudCommandStatus.QUEUED)
                .sorted(Comparator.comparing(CloudCommand::requestedAt))
                .toList();
    }

    public boolean hasPendingCommand(UUID nodeId, CloudCommandType commandType, UUID correlationId) {
        return commands.values().stream()
                .anyMatch(command -> command.nodeId().equals(nodeId)
                        && command.commandType() == commandType
                        && command.correlationId().equals(correlationId)
                        && (command.status() == CloudCommandStatus.CREATED || command.status() == CloudCommandStatus.QUEUED));
    }

    public void savePort(PortAllocation allocation) {
        ports.put(allocation.allocationId(), allocation);
    }

    public boolean portInUse(UUID nodeId, PortProtocol protocol, int publicPort) {
        return ports.values().stream()
                .anyMatch(port -> port.nodeId().equals(nodeId)
                        && port.protocol() == protocol
                        && port.publicPort() == publicPort
                        && port.status() != PortAllocationStatus.RELEASED);
    }

    public void saveBackupPlan(BackupPlan backupPlan) {
        backupPlans.put(backupPlan.backupPlanId(), backupPlan);
    }

    public void saveBackupJob(BackupJob backupJob) {
        backupJobs.put(backupJob.backupId(), backupJob);
    }

    public void saveAlert(CloudAlert alert) {
        alerts.put(alert.alertId(), alert);
    }

    public List<CloudAlert> findAlerts() {
        return new ArrayList<>(alerts.values());
    }
}
