package com.tavall.resourcegame.middleware.authority;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;
import com.tavall.resourcegame.middleware.control.ControlCommand;
import com.tavall.resourcegame.middleware.control.ControlPermission;

import java.util.UUID;

public interface IControlAuthorizationHandler extends IDependencyInjectableInterface {
    AuthorizationResult authorize(ControlCommandRequest request);

    AuthorizationResult authorizeControlCommand(ControlCommand command);

    boolean hasPermission(UUID principalId, ControlPermission permission, ResourceTarget target);

    void requirePermission(UUID principalId, ControlPermission permission, ResourceTarget target);
}
