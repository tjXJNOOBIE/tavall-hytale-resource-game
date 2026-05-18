package org.tavall.control.cloud;

public final class AgentCommandResultReportHandler implements IAgentCommandResultReportHandler, ICloudControlDomain {
    public boolean report(CloudCommandResult result) {
        return getCloudCommandResultHandler().record(result);
    }
}
