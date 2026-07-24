package org.tavall.control.authority;

import org.tavall.dependency.IDependencyInjectableInterface;

import java.util.UUID;

public interface IAuthorityGrantHandler extends IDependencyInjectableInterface {
    ControlAuthority grant(UUID grantedBy, AuthorityGrantRequest request, long nowEpochMillis);
}
