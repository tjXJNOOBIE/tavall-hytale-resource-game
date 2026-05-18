package org.tavall.control.authority;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.util.List;

public interface AuthorizationAuditRepository extends IDependencyInjectableInterface {
    void record(AuthorizationAuditEntry entry);

    List<AuthorizationAuditEntry> findRecent(int limit);
}
