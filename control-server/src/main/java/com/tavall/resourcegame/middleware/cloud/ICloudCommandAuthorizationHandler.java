package com.tavall.resourcegame.middleware.cloud;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import com.tavall.resourcegame.middleware.authority.AuthorizationResult;

import java.time.Instant;
import java.util.UUID;

public interface ICloudCommandAuthorizationHandler extends IDependencyInjectableInterface {
    AuthorizationResult authorize(UUID nodeId, CloudCommandType commandType, String payloadJson, UUID requestedBy, UUID correlationId, Instant now);
}
