package org.tavall.control.cloud;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;

public interface CloudAgentDomain {
    default ObjectMapper getCloudAgentObjectMapper() {
        return DependencyLoaderAccess.findInstance(ObjectMapper.class);
    }

    default IAgentCommandExecutionHandler getAgentCommandExecutionHandler() {
        return DependencyLoaderAccess.findInstance(IAgentCommandExecutionHandler.class);
    }

    default CloudAgentRuntimeConfig getCloudAgentRuntimeConfig() {
        return DependencyLoaderAccess.findInstance(CloudAgentRuntimeConfig.class);
    }

    default ICloudAgentRuntime getCloudAgentRuntime() {
        return DependencyLoaderAccess.findInstance(ICloudAgentRuntime.class);
    }

    default ICloudAgentTransportHandler getCloudAgentTransportHandler() {
        return DependencyLoaderAccess.findInstance(ICloudAgentTransportHandler.class);
    }

    default CloudAgentSecurityConfig getCloudAgentSecurityConfig() {
        return DependencyLoaderAccess.findInstance(CloudAgentSecurityConfig.class);
    }

    default ICloudCommandSignatureHandler getCloudCommandSignatureHandler() {
        return DependencyLoaderAccess.findInstance(ICloudCommandSignatureHandler.class);
    }

    default ISystemdWorkloadRuntimeAdapter getSystemdWorkloadRuntimeAdapter() {
        return DependencyLoaderAccess.findInstance(ISystemdWorkloadRuntimeAdapter.class);
    }

    default ITmuxWorkloadRuntimeAdapter getTmuxWorkloadRuntimeAdapter() {
        return DependencyLoaderAccess.findInstance(ITmuxWorkloadRuntimeAdapter.class);
    }

    default IRawProcessWorkloadRuntimeAdapter getRawProcessWorkloadRuntimeAdapter() {
        return DependencyLoaderAccess.findInstance(IRawProcessWorkloadRuntimeAdapter.class);
    }
}
