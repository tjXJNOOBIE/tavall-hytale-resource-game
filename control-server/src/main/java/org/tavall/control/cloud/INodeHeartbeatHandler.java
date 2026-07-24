package org.tavall.control.cloud;

import org.tavall.dependency.IDependencyInjectableInterface;

import java.time.Instant;
import java.util.UUID;

public interface INodeHeartbeatHandler extends IDependencyInjectableInterface {
    boolean heartbeat(UUID nodeId, Instant now);
}
