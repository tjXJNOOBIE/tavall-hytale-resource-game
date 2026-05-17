package com.tavall.resourcegame.middleware.cloud;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;

import java.time.Instant;
import java.util.UUID;

public interface INodeHeartbeatHandler extends IDependencyInjectableInterface {
    boolean heartbeat(UUID nodeId, Instant now);
}
