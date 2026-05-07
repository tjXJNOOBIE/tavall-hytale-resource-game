package com.tavall.hytale.resourcegame.middleware.authority;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public final class InMemoryAuthorityRepository implements AuthorityRepository {
    private final List<ControlAuthority> authorities = new CopyOnWriteArrayList<>();

    @Override
    public void saveAuthority(ControlAuthority authority) {
        authorities.add(authority);
    }

    @Override
    public List<ControlAuthority> findActiveByPrincipal(UUID principalId, long nowEpochMillis) {
        List<ControlAuthority> activeAuthorities = new ArrayList<>();
        for (ControlAuthority authority : authorities) {
            if (!authority.enabled()) {
                continue;
            }
            if (!authority.principalId().equals(principalId)) {
                continue;
            }
            if (authority.isExpired(nowEpochMillis)) {
                continue;
            }
            activeAuthorities.add(authority);
        }
        return List.copyOf(activeAuthorities);
    }
}
