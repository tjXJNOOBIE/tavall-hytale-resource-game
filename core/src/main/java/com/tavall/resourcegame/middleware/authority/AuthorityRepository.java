package com.tavall.resourcegame.middleware.authority;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;

import java.util.List;
import java.util.UUID;

public interface AuthorityRepository extends IDependencyInjectableInterface {
    void saveAuthority(ControlAuthority authority);

    List<ControlAuthority> findActiveByPrincipal(UUID principalId, long nowEpochMillis);
}
