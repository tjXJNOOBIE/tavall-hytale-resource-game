package org.tavall.control.cloud;

import org.tavall.dependency.IDependencyInjectableInterface;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface IJoinTokenCreationHandler extends IDependencyInjectableInterface {
    String createJoinToken(UUID createdBy, Instant expiresAt, Optional<String> region, Set<CloudNodeCapability> allowedCapabilities);
}
