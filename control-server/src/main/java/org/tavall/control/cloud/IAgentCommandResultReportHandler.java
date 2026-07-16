package org.tavall.control.cloud;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IAgentCommandResultReportHandler extends IDependencyInjectableInterface {
    boolean report(CloudCommandResult result);
}
