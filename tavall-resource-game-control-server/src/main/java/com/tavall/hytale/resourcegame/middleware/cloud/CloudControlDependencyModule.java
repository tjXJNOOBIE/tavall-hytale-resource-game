package com.tavall.hytale.resourcegame.middleware.cloud;

import com.tavall.hytale.resourcegame.dependency.DependencyLoaderAccess;
import com.tavall.hytale.resourcegame.dependency.IDependencyModule;

public final class CloudControlDependencyModule implements IDependencyModule {
    @Override
    public void registerDependencies() {
        DependencyLoaderAccess.findOptionalInstance(CloudRepository.class)
                .orElseGet(() -> register(CloudRepository.class, new InMemoryCloudRepository()));
        DependencyLoaderAccess.registerInstance(ICloudSecretHasher.class, new CloudSecretHasher());
        DependencyLoaderAccess.registerInstance(IJoinTokenCreationHandler.class, new JoinTokenCreationHandler());
        DependencyLoaderAccess.registerInstance(IJoinTokenValidationHandler.class, new JoinTokenValidationHandler());
        DependencyLoaderAccess.registerInstance(INodeRegistrationRequestHandler.class, new NodeRegistrationRequestHandler());
        DependencyLoaderAccess.registerInstance(INodeHeartbeatHandler.class, new NodeHeartbeatHandler());
        DependencyLoaderAccess.registerInstance(INodeSchedulerHandler.class, new NodeSchedulerHandler());
        DependencyLoaderAccess.registerInstance(IWorkloadRequestHandler.class, new WorkloadRequestHandler());
        DependencyLoaderAccess.registerInstance(ICloudCommandCreationHandler.class, new CloudCommandCreationHandler());
        DependencyLoaderAccess.registerInstance(IWorkloadReconciliationHandler.class, new WorkloadReconciliationHandler());
        DependencyLoaderAccess.registerInstance(ICloudCommandResultHandler.class, new CloudCommandResultHandler());
        DependencyLoaderAccess.registerInstance(IPortAllocationHandler.class, new PortAllocationHandler());
        DependencyLoaderAccess.registerInstance(IBackupJobCreationHandler.class, new BackupJobCreationHandler());
        DependencyLoaderAccess.registerInstance(IAlertEvaluationHandler.class, new AlertEvaluationHandler());
        DependencyLoaderAccess.registerInstance(IAgentCommandExecutionHandler.class, new AgentCommandExecutionHandler());
    }

    private <T> T register(Class<T> token, T instance) {
        DependencyLoaderAccess.registerInstance(token, instance);
        return instance;
    }
}
