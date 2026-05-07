package com.tavall.hytale.resourcegame.middleware.cloud;

import com.tavall.hytale.resourcegame.dependency.IDependencyInjectableInterface;

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

    List<CloudCommand> findPendingCommands(UUID nodeId);

    boolean hasPendingCommand(UUID nodeId, CloudCommandType commandType, UUID correlationId);

    void savePort(PortAllocation allocation);

    boolean portInUse(UUID nodeId, PortProtocol protocol, int publicPort);

    void saveBackupPlan(BackupPlan backupPlan);

    void saveBackupJob(BackupJob backupJob);

    void saveAlert(CloudAlert alert);

    List<CloudAlert> findAlerts();
}
