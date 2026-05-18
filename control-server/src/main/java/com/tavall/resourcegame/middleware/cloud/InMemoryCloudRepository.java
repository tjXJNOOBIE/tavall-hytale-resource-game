package org.tavall.control.cloud;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryCloudRepository implements CloudRepository {
    private final Map<UUID, JoinToken> joinTokens = new ConcurrentHashMap<>();
    private final Map<UUID, CloudNode> nodes = new ConcurrentHashMap<>();
    private final Map<UUID, NodeIdentity> identities = new ConcurrentHashMap<>();
    private final Map<UUID, NodeAgentRuntime> agents = new ConcurrentHashMap<>();
    private final Map<UUID, CloudWorkload> workloads = new ConcurrentHashMap<>();
    private final Map<UUID, CloudCommand> commands = new ConcurrentHashMap<>();
    private final Map<UUID, PortAllocation> ports = new ConcurrentHashMap<>();
    private final Map<UUID, CloudVolume> volumes = new ConcurrentHashMap<>();
    private final Map<UUID, CloudFirewallRule> firewallRules = new ConcurrentHashMap<>();
    private final Map<UUID, ReverseProxyRoute> reverseProxyRoutes = new ConcurrentHashMap<>();
    private final Map<UUID, DnsRecord> dnsRecords = new ConcurrentHashMap<>();
    private final Map<UUID, BackupPlan> backupPlans = new ConcurrentHashMap<>();
    private final Map<UUID, BackupJob> backupJobs = new ConcurrentHashMap<>();
    private final Map<UUID, RestoreJob> restoreJobs = new ConcurrentHashMap<>();
    private final Map<UUID, List<NodeMetricSnapshot>> nodeMetrics = new ConcurrentHashMap<>();
    private final Map<UUID, List<WorkloadMetricSnapshot>> workloadMetrics = new ConcurrentHashMap<>();
    private final Map<UUID, CloudAlert> alerts = new ConcurrentHashMap<>();

    public void saveJoinToken(JoinToken joinToken) {
        joinTokens.put(joinToken.joinTokenId(), joinToken);
    }

    public Optional<JoinToken> findJoinTokenByHash(String tokenHash) {
        return joinTokens.values().stream()
                .filter(token -> token.tokenHash().equals(tokenHash))
                .findFirst();
    }

    public void saveNode(CloudNode node) {
        nodes.put(node.nodeId(), node);
    }

    public Optional<CloudNode> findNode(UUID nodeId) {
        return Optional.ofNullable(nodes.get(nodeId));
    }

    public List<CloudNode> findNodes() {
        return nodes.values().stream()
                .sorted(Comparator.comparing(CloudNode::hostname))
                .toList();
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
        return workloads.values().stream()
                .sorted(Comparator.comparing(CloudWorkload::name))
                .toList();
    }

    public void saveCommand(CloudCommand command) {
        commands.put(command.commandId(), command);
    }

    public Optional<CloudCommand> findCommand(UUID commandId) {
        return Optional.ofNullable(commands.get(commandId));
    }

    public List<CloudCommand> findCommands() {
        return commands.values().stream()
                .sorted(Comparator.comparing(CloudCommand::requestedAt))
                .toList();
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
                        && (command.status() == CloudCommandStatus.CREATED || command.status() == CloudCommandStatus.QUEUED
                        || command.status() == CloudCommandStatus.SENT || command.status() == CloudCommandStatus.ACKNOWLEDGED
                        || command.status() == CloudCommandStatus.RUNNING));
    }

    public void savePort(PortAllocation allocation) {
        ports.put(allocation.allocationId(), allocation);
    }

    public List<PortAllocation> findPorts() {
        return new ArrayList<>(ports.values());
    }

    public boolean portInUse(UUID nodeId, PortProtocol protocol, int publicPort) {
        return ports.values().stream()
                .anyMatch(port -> port.nodeId().equals(nodeId)
                        && port.protocol() == protocol
                        && port.publicPort() == publicPort
                        && port.status() != PortAllocationStatus.RELEASED);
    }

    public void saveVolume(CloudVolume volume) {
        volumes.put(volume.volumeId(), volume);
    }

    public List<CloudVolume> findVolumes() {
        return new ArrayList<>(volumes.values());
    }

    public void saveFirewallRule(CloudFirewallRule firewallRule) {
        firewallRules.put(firewallRule.ruleId(), firewallRule);
    }

    public List<CloudFirewallRule> findFirewallRules() {
        return new ArrayList<>(firewallRules.values());
    }

    public void saveReverseProxyRoute(ReverseProxyRoute route) {
        reverseProxyRoutes.put(route.routeId(), route);
    }

    public List<ReverseProxyRoute> findReverseProxyRoutes() {
        return new ArrayList<>(reverseProxyRoutes.values());
    }

    public void saveDnsRecord(DnsRecord record) {
        dnsRecords.put(record.recordId(), record);
    }

    public List<DnsRecord> findDnsRecords() {
        return new ArrayList<>(dnsRecords.values());
    }

    public void saveBackupPlan(BackupPlan backupPlan) {
        backupPlans.put(backupPlan.backupPlanId(), backupPlan);
    }

    public List<BackupPlan> findBackupPlans() {
        return new ArrayList<>(backupPlans.values());
    }

    public void saveBackupJob(BackupJob backupJob) {
        backupJobs.put(backupJob.backupId(), backupJob);
    }

    public Optional<BackupJob> findBackupJob(UUID backupId) {
        return Optional.ofNullable(backupJobs.get(backupId));
    }

    public List<BackupJob> findBackupJobs() {
        return new ArrayList<>(backupJobs.values());
    }

    public void saveRestoreJob(RestoreJob restoreJob) {
        restoreJobs.put(restoreJob.restoreId(), restoreJob);
    }

    public Optional<RestoreJob> findRestoreJob(UUID restoreId) {
        return Optional.ofNullable(restoreJobs.get(restoreId));
    }

    public List<RestoreJob> findRestoreJobs() {
        return new ArrayList<>(restoreJobs.values());
    }

    public void saveNodeMetric(NodeMetricSnapshot snapshot) {
        nodeMetrics.compute(snapshot.nodeId(), (nodeId, snapshots) -> {
            List<NodeMetricSnapshot> updated = snapshots == null ? new ArrayList<>() : new ArrayList<>(snapshots);
            updated.add(snapshot);
            updated.sort(Comparator.comparing(NodeMetricSnapshot::collectedAt));
            return updated;
        });
    }

    public List<NodeMetricSnapshot> findNodeMetrics(UUID nodeId) {
        return nodeMetrics.getOrDefault(nodeId, List.of()).stream()
                .sorted(Comparator.comparing(NodeMetricSnapshot::collectedAt))
                .toList();
    }

    public void saveWorkloadMetric(WorkloadMetricSnapshot snapshot) {
        workloadMetrics.compute(snapshot.workloadId(), (workloadId, snapshots) -> {
            List<WorkloadMetricSnapshot> updated = snapshots == null ? new ArrayList<>() : new ArrayList<>(snapshots);
            updated.add(snapshot);
            updated.sort(Comparator.comparing(WorkloadMetricSnapshot::collectedAt));
            return updated;
        });
    }

    public List<WorkloadMetricSnapshot> findWorkloadMetrics(UUID workloadId) {
        return workloadMetrics.getOrDefault(workloadId, List.of()).stream()
                .sorted(Comparator.comparing(WorkloadMetricSnapshot::collectedAt))
                .toList();
    }

    public void saveAlert(CloudAlert alert) {
        alerts.put(alert.alertId(), alert);
    }

    public List<CloudAlert> findAlerts() {
        return alerts.values().stream()
                .sorted(Comparator.comparing(CloudAlert::createdAt))
                .toList();
    }
}
