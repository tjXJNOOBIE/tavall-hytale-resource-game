package com.tavall.hytale.resourcegame.middleware.authority;

import java.util.List;

public interface AuthorizationAuditRepository {
    void record(AuthorizationAuditEntry entry);

    List<AuthorizationAuditEntry> findRecent(int limit);
}
