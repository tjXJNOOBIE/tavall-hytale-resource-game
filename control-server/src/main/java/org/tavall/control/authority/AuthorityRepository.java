package org.tavall.control.authority;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.util.List;
import java.util.UUID;

public interface AuthorityRepository extends IDependencyInjectableInterface {
    void saveAuthority(ControlAuthority authority);

    List<ControlAuthority> findActiveByPrincipal(UUID principalId, long nowEpochMillis);
}
