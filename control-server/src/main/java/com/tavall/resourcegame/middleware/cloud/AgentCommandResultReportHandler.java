package com.tavall.resourcegame.middleware.cloud;

public final class AgentCommandResultReportHandler implements IAgentCommandResultReportHandler, ICloudControlDomain {
    public boolean report(CloudCommandResult result) {
        return getCloudCommandResultHandler().record(result);
    }
}
