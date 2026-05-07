package com.tavall.hytale.resourcegame.middleware.cloud;

import com.tavall.hytale.resourcegame.dependency.IDependencyInjectableInterface;

import java.time.Instant;

public interface IAgentCommandExecutionHandler extends IDependencyInjectableInterface {
    CloudCommandResult execute(CloudCommand command, Instant now);
}
