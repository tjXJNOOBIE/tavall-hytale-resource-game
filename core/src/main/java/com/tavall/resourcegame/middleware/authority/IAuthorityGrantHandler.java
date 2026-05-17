package com.tavall.resourcegame.middleware.authority;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;

import java.util.UUID;

public interface IAuthorityGrantHandler extends IDependencyInjectableInterface {
    ControlAuthority grant(UUID grantedBy, AuthorityGrantRequest request, long nowEpochMillis);
}
