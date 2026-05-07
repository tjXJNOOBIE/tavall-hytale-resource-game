package com.tavall.hytale.resourcegame.middleware.authority;

import java.util.Optional;

public interface PermissionPolicyRepository {
    ControlPermissionPolicy requiredPolicy(CloudCommandType commandType);

    Optional<ControlPermissionPolicy> findPolicy(CloudCommandType commandType);
}
