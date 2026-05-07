package com.tavall.hytale.resourcegame.middleware.cloud;

import com.tavall.hytale.resourcegame.dependency.IDependencyInjectableInterface;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public interface IAlertEvaluationHandler extends IDependencyInjectableInterface {
    Optional<CloudAlert> evaluateHeartbeat(CloudNode node, Instant now, Duration threshold);
}
