package org.tavall.control.cloud;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.time.Instant;
import java.util.UUID;

public interface INodeHeartbeatHandler extends IDependencyInjectableInterface {
    boolean heartbeat(UUID nodeId, Instant now);
}
