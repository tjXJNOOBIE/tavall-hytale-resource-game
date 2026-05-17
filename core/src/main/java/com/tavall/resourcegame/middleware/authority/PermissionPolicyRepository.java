package com.tavall.resourcegame.middleware.authority;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;

import java.util.Optional;

public interface PermissionPolicyRepository extends IDependencyInjectableInterface {
    ControlPermissionPolicy requiredPolicy(CloudCommandType commandType);

    Optional<ControlPermissionPolicy> findPolicy(CloudCommandType commandType);
}
