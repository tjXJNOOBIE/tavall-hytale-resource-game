package org.tavall.control.cloud;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.time.Instant;

public interface IAgentCommandExecutionHandler extends IDependencyInjectableInterface {
    CloudCommandResult execute(CloudCommand command, Instant now);
}
