package com.tavall.hytale.resourcegame.middleware.cloud;

import com.tavall.hytale.resourcegame.dependency.IDependencyInjectableInterface;

import java.time.Instant;

public interface INodeRegistrationRequestHandler extends IDependencyInjectableInterface {
    NodeRegistrationResult register(NodeRegistrationRequest request, Instant now);
}
