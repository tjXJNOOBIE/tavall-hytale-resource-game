package org.tavall.control.cloud;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.time.Instant;

public interface INodeRegistrationRequestHandler extends IDependencyInjectableInterface {
    NodeRegistrationResult register(NodeRegistrationRequest request, Instant now);
}
