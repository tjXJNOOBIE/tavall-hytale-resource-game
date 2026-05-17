package com.tavall.resourcegame.middleware.cloud;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import com.tjxjnoobie.api.dependency.IDependencyModule;

public final class CloudAgentDependencyModule implements IDependencyModule {
    @Override
    public void registerDependencies() {
        DependencyLoaderAccess.findOptionalInstance(ObjectMapper.class)
                .orElseGet(() -> register(ObjectMapper.class, new ObjectMapper().findAndRegisterModules()));
        DependencyLoaderAccess.findOptionalInstance(CloudAgentRuntimeConfig.class)
                .orElseGet(() -> register(CloudAgentRuntimeConfig.class, CloudAgentRuntimeConfig.fromEnvironment(System.getenv())));
        DependencyLoaderAccess.findOptionalInstance(CloudAgentSecurityConfig.class)
                .orElseGet(() -> register(CloudAgentSecurityConfig.class, CloudAgentSecurityConfig.fromEnvironment(System.getenv())));
        DependencyLoaderAccess.findOptionalInstance(ICloudCommandSignatureHandler.class)
                .orElseGet(() -> register(ICloudCommandSignatureHandler.class, new CloudCommandSignatureHandler()));
        DependencyLoaderAccess.findOptionalInstance(ICloudAgentTransportHandler.class)
                .orElseGet(() -> register(ICloudAgentTransportHandler.class, new CloudAgentHttpTransportHandler()));
        DependencyLoaderAccess.findOptionalInstance(ICloudAgentRuntime.class)
                .orElseGet(() -> register(ICloudAgentRuntime.class, new CloudAgentRuntime()));
        DependencyLoaderAccess.findOptionalInstance(IAgentCommandExecutionHandler.class)
                .orElseGet(() -> register(IAgentCommandExecutionHandler.class, new AgentCommandExecutionHandler()));
        DependencyLoaderAccess.findOptionalInstance(ISystemdWorkloadRuntimeAdapter.class)
                .orElseGet(() -> register(ISystemdWorkloadRuntimeAdapter.class, new SystemdWorkloadRuntimeAdapter()));
        DependencyLoaderAccess.findOptionalInstance(ITmuxWorkloadRuntimeAdapter.class)
                .orElseGet(() -> register(ITmuxWorkloadRuntimeAdapter.class, new TmuxWorkloadRuntimeAdapter()));
        DependencyLoaderAccess.findOptionalInstance(IRawProcessWorkloadRuntimeAdapter.class)
                .orElseGet(() -> register(IRawProcessWorkloadRuntimeAdapter.class, new RawProcessWorkloadRuntimeAdapter()));
    }

    private <T> T register(Class<T> token, T instance) {
        DependencyLoaderAccess.registerInstance(token, instance);
        return instance;
    }
}
