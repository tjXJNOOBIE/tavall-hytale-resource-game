package org.tavall.control.authority;

import org.tavall.dependency.IDependencyInjectableInterface;

import java.util.Optional;

public interface PermissionPolicyRepository extends IDependencyInjectableInterface {
    ControlPermissionPolicy requiredPolicy(CloudCommandType commandType);

    Optional<ControlPermissionPolicy> findPolicy(CloudCommandType commandType);
}
