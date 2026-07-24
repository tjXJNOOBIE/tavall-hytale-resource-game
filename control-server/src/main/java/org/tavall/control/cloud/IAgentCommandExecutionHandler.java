package org.tavall.control.cloud;

import org.tavall.dependency.IDependencyInjectableInterface;

import java.time.Instant;

public interface IAgentCommandExecutionHandler extends IDependencyInjectableInterface {
    CloudCommandResult execute(CloudCommand command, Instant now);
}
