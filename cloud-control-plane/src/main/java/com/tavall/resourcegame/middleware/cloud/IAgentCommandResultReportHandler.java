package com.tavall.resourcegame.middleware.cloud;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;

public interface IAgentCommandResultReportHandler extends IDependencyInjectableInterface {
    boolean report(CloudCommandResult result);
}
