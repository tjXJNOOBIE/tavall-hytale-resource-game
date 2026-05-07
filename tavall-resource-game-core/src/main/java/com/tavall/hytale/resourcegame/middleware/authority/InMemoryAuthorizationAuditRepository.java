package com.tavall.hytale.resourcegame.middleware.authority;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class InMemoryAuthorizationAuditRepository implements AuthorizationAuditRepository {
    private final List<AuthorizationAuditEntry> entries = new CopyOnWriteArrayList<>();

    @Override
    public void record(AuthorizationAuditEntry entry) {
        entries.add(entry);
    }

    @Override
    public List<AuthorizationAuditEntry> findRecent(int limit) {
        int resolvedLimit = Math.max(0, limit);
        return entries.stream()
                .sorted(Comparator.comparingLong(AuthorizationAuditEntry::auditedAtEpochMillis).reversed())
                .limit(resolvedLimit)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }
}
