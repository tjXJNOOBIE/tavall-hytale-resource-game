package com.tavall.resourcegame.middleware.cloud;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

public interface IAgentCommandResultReportHandler extends IDependencyInjectableInterface {
    boolean report(CloudCommandResult result);
}
