package org.tavall.control.cloud;

import org.tavall.control.authority.AuthorityScope;
import org.tavall.control.authority.ControlAuthority;
import org.tavall.control.authority.ControlAuthorityLevel;
import org.tavall.control.authority.ControlAuthorityDomain;
import org.tavall.control.runtime.ControlOperator;
import org.tavall.control.runtime.ControlPermission;

import java.time.Instant;
import java.util.EnumSet;
import java.util.UUID;

public final class CloudOwnerAuthorityBootstrapHandler implements ICloudOwnerAuthorityBootstrapHandler, ControlAuthorityDomain {
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
