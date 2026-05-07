package com.tavall.hytale.resourcegame.middleware.authority;

import com.tavall.hytale.resourcegame.dependency.IDependencyInjectableInterface;

import java.util.UUID;

public interface IAuthorityGrantHandler extends IDependencyInjectableInterface {
    ControlAuthority grant(UUID grantedBy, AuthorityGrantRequest request, long nowEpochMillis);
}
