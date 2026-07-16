package org.tavall.control.cloud;

public final class AgentCommandResultReportHandler implements IAgentCommandResultReportHandler, CloudControlDomain {
    public boolean report(CloudCommandResult result) {
        return getCloudCommandResultHandler().record(result);
    }
}
