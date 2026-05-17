package com.tavall.resourcegame.middleware.control;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryControlCommandAuditLogRepository implements ControlCommandAuditLogRepository {
    private final Map<UUID, ControlCommandAuditLog> auditLogsById = new ConcurrentHashMap<>();

    @Override
    public ControlCommandAuditLog saveAuditLog(ControlCommandAuditLog auditLog) {
        auditLogsById.put(auditLog.auditLogId(), auditLog);
        return auditLog;
    }

    @Override
    public Optional<ControlCommandAuditLog> findAuditLog(UUID auditLogId) {
        return Optional.ofNullable(auditLogsById.get(auditLogId));
    }

    @Override
    public List<ControlCommandAuditLog> findAuditLogsForCommand(ControlCommandId commandId) {
        return auditLogsById.values().stream()
                .filter(auditLog -> auditLog.commandId().equals(commandId))
                .sorted(Comparator.comparing(ControlCommandAuditLog::createdAt).reversed())
                .toList();
    }

    @Override
    public List<ControlCommandAuditLog> findRecentAuditLogs(int limit) {
        return auditLogsById.values().stream()
                .sorted(Comparator.comparing(ControlCommandAuditLog::createdAt).reversed())
                .limit(Math.max(0, limit))
                .toList();
    }
}
