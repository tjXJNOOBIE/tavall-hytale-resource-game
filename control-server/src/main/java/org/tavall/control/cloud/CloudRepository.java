package org.tavall.control.cloud;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CloudRepository extends IDependencyInjectableInterface {
    void saveJoinToken(JoinToken joinToken);

    Optional<JoinToken> findJoinTokenByHash(String tokenHash);

    void saveNode(CloudNode node);

    Optional<CloudNode> findNode(UUID nodeId);

    List<CloudNode> findNodes();

    void saveIdentity(NodeIdentity identity);

    Optional<NodeIdentity> findIdentity(UUID nodeId);

    void saveAgent(NodeAgentRuntime agent);

    void saveWorkload(CloudWorkload workload);

    Optional<CloudWorkload> findWorkload(UUID workloadId);

    List<CloudWorkload> findWorkloads();

    void saveCommand(CloudCommand command);

    Optional<CloudCommand> findCommand(UUID commandId);

    List<CloudCommand> findCommands();

    List<CloudCommand> findPendingCommands(UUID nodeId);

    boolean hasPendingCommand(UUID nodeId, CloudCommandType commandType, UUID correlationId);

    void savePort(PortAllocation allocation);

    List<PortAllocation> findPorts();

    boolean portInUse(UUID nodeId, PortProtocol protocol, int publicPort);

    void saveVolume(CloudVolume volume);

    List<CloudVolume> findVolumes();

    void saveFirewallRule(CloudFirewallRule firewallRule);

    List<CloudFirewallRule> findFirewallRules();

    void saveReverseProxyRoute(ReverseProxyRoute route);

    List<ReverseProxyRoute> findReverseProxyRoutes();

    void saveDnsRecord(DnsRecord record);

    List<DnsRecord> findDnsRecords();

    void saveBackupPlan(BackupPlan backupPlan);

    List<BackupPlan> findBackupPlans();

    void saveBackupJob(BackupJob backupJob);

    Optional<BackupJob> findBackupJob(UUID backupId);

    List<BackupJob> findBackupJobs();

    void saveRestoreJob(RestoreJob restoreJob);

    Optional<RestoreJob> findRestoreJob(UUID restoreId);

    List<RestoreJob> findRestoreJobs();

    void saveNodeMetric(NodeMetricSnapshot snapshot);

    List<NodeMetricSnapshot> findNodeMetrics(UUID nodeId);

    void saveWorkloadMetric(WorkloadMetricSnapshot snapshot);

    List<WorkloadMetricSnapshot> findWorkloadMetrics(UUID workloadId);

    void saveAlert(CloudAlert alert);

    List<CloudAlert> findAlerts();
}
