package org.tavall.control.cloud;

import org.tavall.dependency.IDependencyInjectableInterface;
import org.tavall.control.authority.AuthorizationResult;

import java.time.Instant;
import java.util.UUID;

public interface ICloudCommandAuthorizationHandler extends IDependencyInjectableInterface {
    AuthorizationResult authorize(UUID nodeId, CloudCommandType commandType, String payloadJson, UUID requestedBy, UUID correlationId, Instant now);
}
