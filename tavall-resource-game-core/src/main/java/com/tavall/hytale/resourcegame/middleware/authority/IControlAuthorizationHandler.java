package com.tavall.hytale.resourcegame.middleware.authority;

import com.tavall.hytale.resourcegame.dependency.IDependencyInjectableInterface;
import com.tavall.hytale.resourcegame.middleware.control.ControlCommand;
import com.tavall.hytale.resourcegame.middleware.control.ControlPermission;

import java.util.UUID;

public interface IControlAuthorizationHandler extends IDependencyInjectableInterface {
    AuthorizationResult authorize(ControlCommandRequest request);

    AuthorizationResult authorizeControlCommand(ControlCommand command);

    boolean hasPermission(UUID principalId, ControlPermission permission, ResourceTarget target);

    void requirePermission(UUID principalId, ControlPermission permission, ResourceTarget target);
}
