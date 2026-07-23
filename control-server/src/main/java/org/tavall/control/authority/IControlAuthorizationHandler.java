package org.tavall.control.authority;

import org.tavall.dependency.IDependencyInjectableInterface;
import org.tavall.control.runtime.ControlCommand;
import org.tavall.control.runtime.ControlPermission;

import java.util.UUID;

public interface IControlAuthorizationHandler extends IDependencyInjectableInterface {
    AuthorizationResult authorize(ControlCommandRequest request);

    AuthorizationResult authorizeControlCommand(ControlCommand command);

    boolean hasPermission(UUID principalId, ControlPermission permission, ResourceTarget target);

    void requirePermission(UUID principalId, ControlPermission permission, ResourceTarget target);
}
