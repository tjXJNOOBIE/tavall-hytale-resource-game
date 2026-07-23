package org.tavall.control.cloud;

import org.tavall.dependency.IDependencyInjectableInterface;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface IAgentCommandPollHandler extends IDependencyInjectableInterface {
    List<CloudCommand> poll(UUID nodeId, int maxCommands, Instant now);
}
