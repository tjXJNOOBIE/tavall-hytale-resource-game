package com.tavall.hytale.resourcegame.middleware.cloud;

import com.tavall.hytale.resourcegame.dependency.IDependencyInjectableInterface;

import java.time.Instant;
import java.util.UUID;

public interface INodeHeartbeatHandler extends IDependencyInjectableInterface {
    boolean heartbeat(UUID nodeId, Instant now);
}
