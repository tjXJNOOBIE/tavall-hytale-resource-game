package com.tavall.hytale.resourcegame.middleware.cloud;

import com.tavall.hytale.resourcegame.dependency.IDependencyInjectableInterface;

import java.time.Instant;
import java.util.Optional;

public interface IJoinTokenValidationHandler extends IDependencyInjectableInterface {
    Optional<JoinToken> validate(String plaintextToken, Instant now);
}
