package com.tavall.resourcegame.middleware.cloud;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface IJoinTokenCreationHandler extends IDependencyInjectableInterface {
    String createJoinToken(UUID createdBy, Instant expiresAt, Optional<String> region, Set<CloudNodeCapability> allowedCapabilities);
}
