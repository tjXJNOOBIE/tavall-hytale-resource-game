package org.tavall.control.cloud;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import com.tjxjnoobie.api.dependency.IDependencyModule;
import org.tavall.control.authority.ControlAuthorityDependencyModule;

public final class CloudControlDependencyModule implements IDependencyModule {
    @Override
    public void registerDependencies() {
        new ControlAuthorityDependencyModule().registerDependencies();
        DependencyLoaderAccess.findOptionalInstance(ObjectMapper.class)
                .orElseGet(() -> register(ObjectMapper.class, new ObjectMapper().findAndRegisterModules()));
        DependencyLoaderAccess.findOptionalInstance(CloudRepository.class)
                .orElseGet(() -> register(CloudRepository.class, new InMemoryCloudRepository()));
        DependencyLoaderAccess.findOptionalInstance(CloudControlPlaneFilesystemLayout.class)
                .orElseGet(() -> register(CloudControlPlaneFilesystemLayout.class, CloudControlPlaneFilesystemLayout.fromEnvironment()));
        DependencyLoaderAccess.registerInstance(ICloudSecretHasher.class, new CloudSecretHasher());
        DependencyLoaderAccess.registerInstance(IJoinTokenCreationHandler.class, new JoinTokenCreationHandler());
        DependencyLoaderAccess.registerInstance(IJoinTokenValidationHandler.class, new JoinTokenValidationHandler());
        DependencyLoaderAccess.registerInstance(INodeRegistrationRequestHandler.class, new NodeRegistrationRequestHandler());
        DependencyLoaderAccess.registerInstance(INodeHeartbeatHandler.class, new NodeHeartbeatHandler());
        DependencyLoaderAccess.registerInstance(INodeSchedulerHandler.class, new NodeSchedulerHandler());
        DependencyLoaderAccess.registerInstance(IWorkloadRequestHandler.class, new WorkloadRequestHandler());
        DependencyLoaderAccess.registerInstance(ICloudCommandValidationHandler.class, new CloudCommandValidationHandler());
        DependencyLoaderAccess.registerInstance(ICloudCommandAuthorizationHandler.class, new CloudCommandAuthorizationHandler());
        DependencyLoaderAccess.registerInstance(ICloudCommandCreationHandler.class, new CloudCommandCreationHandler());
        DependencyLoaderAccess.registerInstance(IWorkloadReconciliationHandler.class, new WorkloadReconciliationHandler());
        DependencyLoaderAccess.registerInstance(ICloudCommandResultHandler.class, new CloudCommandResultHandler());
        DependencyLoaderAccess.registerInstance(IAgentCommandPollHandler.class, new AgentCommandPollHandler());
        DependencyLoaderAccess.registerInstance(IAgentCommandResultReportHandler.class, new AgentCommandResultReportHandler());
        DependencyLoaderAccess.registerInstance(IPortAllocationHandler.class, new PortAllocationHandler());
        DependencyLoaderAccess.registerInstance(IBackupJobCreationHandler.class, new BackupJobCreationHandler());
        DependencyLoaderAccess.registerInstance(IBackupResultHandler.class, new BackupResultHandler());
        DependencyLoaderAccess.registerInstance(IBackupVerificationHandler.class, new BackupVerificationHandler());
        DependencyLoaderAccess.registerInstance(IRestoreJobCreationHandler.class, new RestoreJobCreationHandler());
        DependencyLoaderAccess.registerInstance(IRestoreResultHandler.class, new RestoreResultHandler());
        DependencyLoaderAccess.registerInstance(IAlertEvaluationHandler.class, new AlertEvaluationHandler());
        DependencyLoaderAccess.registerInstance(INodeMetricIngestionHandler.class, new NodeMetricIngestionHandler());
        DependencyLoaderAccess.registerInstance(IWorkloadMetricIngestionHandler.class, new WorkloadMetricIngestionHandler());
        DependencyLoaderAccess.registerInstance(ICloudControlCliHandler.class, new CloudControlCliHandler());
        DependencyLoaderAccess.registerInstance(ICloudControlPanelViewHandler.class, new CloudControlPanelViewHandler());
        DependencyLoaderAccess.registerInstance(ICloudOwnerAuthorityBootstrapHandler.class, new CloudOwnerAuthorityBootstrapHandler());
        DependencyLoaderAccess.registerInstance(IVolumeProvisioningHandler.class, new VolumeProvisioningHandler());
        DependencyLoaderAccess.registerInstance(IFirewallRuleDesiredStateHandler.class, new FirewallRuleDesiredStateHandler());
        DependencyLoaderAccess.registerInstance(IReverseProxyRouteCreationHandler.class, new ReverseProxyRouteCreationHandler());
        DependencyLoaderAccess.registerInstance(IDnsRecordDesiredStateHandler.class, new DnsRecordDesiredStateHandler());
        DependencyLoaderAccess.registerInstance(IMinecraftServerSnapshotIngressHandler.class, new MinecraftServerSnapshotIngressHandler());
    }

    private <T> T register(Class<T> token, T instance) {
        DependencyLoaderAccess.registerInstance(token, instance);
        return instance;
    }
}
