package com.tavall.hytale.resourcegame.middleware.authority;

import java.util.List;
import java.util.UUID;

public interface AuthorityRepository {
    void saveAuthority(ControlAuthority authority);

    List<ControlAuthority> findActiveByPrincipal(UUID principalId, long nowEpochMillis);
}
