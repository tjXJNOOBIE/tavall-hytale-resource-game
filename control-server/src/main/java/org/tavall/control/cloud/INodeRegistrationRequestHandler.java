package org.tavall.control.cloud;

import org.tavall.dependency.IDependencyInjectableInterface;

import java.time.Instant;

public interface INodeRegistrationRequestHandler extends IDependencyInjectableInterface {
    NodeRegistrationResult register(NodeRegistrationRequest request, Instant now);
}
