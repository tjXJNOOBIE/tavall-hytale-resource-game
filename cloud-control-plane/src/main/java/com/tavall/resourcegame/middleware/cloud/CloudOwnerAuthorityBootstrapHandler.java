package com.tavall.resourcegame.middleware.cloud;

import com.tavall.resourcegame.middleware.authority.AuthorityScope;
import com.tavall.resourcegame.middleware.authority.ControlAuthority;
import com.tavall.resourcegame.middleware.authority.ControlAuthorityLevel;
import com.tavall.resourcegame.middleware.authority.IControlAuthorityDomain;
import com.tavall.resourcegame.middleware.control.ControlOperator;
import com.tavall.resourcegame.middleware.control.ControlPermission;

import java.time.Instant;
import java.util.EnumSet;
import java.util.UUID;

public final class CloudOwnerAuthorityBootstrapHandler implements ICloudOwnerAuthorityBootstrapHandler, IControlAuthorityDomain {
    /**
     * Local in-memory runtimes represent the owner console, so they seed a durable-style C5 grant instead of bypassing authorization.
     */
    @Override
    public void ensureLocalOwnerAuthority(Instant now) {
        UUID localOwnerId = ControlOperator.localOwner(now).operatorId();
        if (!getAuthorityRepository().findActiveByPrincipal(localOwnerId, now.toEpochMilli()).isEmpty()) {
            return;
        }
        getAuthorityRepository().saveAuthority(ControlAuthority.enabled(
                localOwnerId,
                ControlAuthorityLevel.C5_GLOBAL_AUTHORITY,
                AuthorityScope.global(),
                EnumSet.allOf(ControlPermission.class),
                localOwnerId,
                now.toEpochMilli(),
                true,
                true
        ));
    }
}
