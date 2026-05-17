package com.tavall.resourcegame.middleware.authority;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;

import java.util.List;

public interface AuthorizationAuditRepository extends IDependencyInjectableInterface {
    void record(AuthorizationAuditEntry entry);

    List<AuthorizationAuditEntry> findRecent(int limit);
}
