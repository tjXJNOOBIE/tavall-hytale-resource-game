package com.tavall.resourcegame.middleware.cloud;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;

import java.time.Instant;

public interface INodeRegistrationRequestHandler extends IDependencyInjectableInterface {
    NodeRegistrationResult register(NodeRegistrationRequest request, Instant now);
}
