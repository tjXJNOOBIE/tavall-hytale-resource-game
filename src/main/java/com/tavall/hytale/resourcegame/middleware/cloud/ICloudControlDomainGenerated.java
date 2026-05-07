package com.tavall.hytale.resourcegame.middleware.cloud;

import com.tavall.hytale.resourcegame.dependency.DependencyLoaderAccess;

public interface ICloudControlDomainGenerated {
    default CloudRepository getCloudRepository() {
        return DependencyLoaderAccess.findInstance(CloudRepository.class);
    }

    default ICloudSecretHasher getCloudSecretHasher() {
        return DependencyLoaderAccess.findInstance(ICloudSecretHasher.class);
    }

    default IJoinTokenValidationHandler getJoinTokenValidationHandler() {
        return DependencyLoaderAccess.findInstance(IJoinTokenValidationHandler.class);
    }

    default IJoinTokenCreationHandler getJoinTokenCreationHandler() {
        return DependencyLoaderAccess.findInstance(IJoinTokenCreationHandler.class);
    }

    default INodeRegistrationRequestHandler getNodeRegistrationRequestHandler() {
        return DependencyLoaderAccess.findInstance(INodeRegistrationRequestHandler.class);
    }

    default INodeHeartbeatHandler getNodeHeartbeatHandler() {
        return DependencyLoaderAccess.findInstance(INodeHeartbeatHandler.class);
    }

    default INodeSchedulerHandler getNodeSchedulerHandler() {
        return DependencyLoaderAccess.findInstance(INodeSchedulerHandler.class);
    }

    default IWorkloadRequestHandler getWorkloadRequestHandler() {
        return DependencyLoaderAccess.findInstance(IWorkloadRequestHandler.class);
    }

    default ICloudCommandCreationHandler getCloudCommandCreationHandler() {
        return DependencyLoaderAccess.findInstance(ICloudCommandCreationHandler.class);
    }

    default IWorkloadReconciliationHandler getWorkloadReconciliationHandler() {
        return DependencyLoaderAccess.findInstance(IWorkloadReconciliationHandler.class);
    }

    default ICloudCommandResultHandler getCloudCommandResultHandler() {
        return DependencyLoaderAccess.findInstance(ICloudCommandResultHandler.class);
    }

    default IPortAllocationHandler getPortAllocationHandler() {
        return DependencyLoaderAccess.findInstance(IPortAllocationHandler.class);
    }

    default IBackupJobCreationHandler getBackupJobCreationHandler() {
        return DependencyLoaderAccess.findInstance(IBackupJobCreationHandler.class);
    }

    default IAlertEvaluationHandler getAlertEvaluationHandler() {
        return DependencyLoaderAccess.findInstance(IAlertEvaluationHandler.class);
    }

    default IAgentCommandExecutionHandler getAgentCommandExecutionHandler() {
        return DependencyLoaderAccess.findInstance(IAgentCommandExecutionHandler.class);
    }
}
