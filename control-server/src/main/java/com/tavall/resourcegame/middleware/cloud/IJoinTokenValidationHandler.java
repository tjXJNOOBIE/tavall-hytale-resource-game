package com.tavall.resourcegame.middleware.cloud;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.time.Instant;
import java.util.Optional;

public interface IJoinTokenValidationHandler extends IDependencyInjectableInterface {
    Optional<JoinToken> validate(String plaintextToken, Instant now);
}
