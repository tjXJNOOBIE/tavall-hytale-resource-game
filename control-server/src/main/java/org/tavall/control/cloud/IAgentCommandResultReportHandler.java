package org.tavall.control.cloud;

import org.tavall.dependency.IDependencyInjectableInterface;

public interface IAgentCommandResultReportHandler extends IDependencyInjectableInterface {
    boolean report(CloudCommandResult result);
}
