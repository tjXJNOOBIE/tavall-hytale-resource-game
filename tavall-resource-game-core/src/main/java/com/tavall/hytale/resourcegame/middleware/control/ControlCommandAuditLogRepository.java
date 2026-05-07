package com.tavall.hytale.resourcegame.middleware.control;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ControlCommandAuditLogRepository {
    ControlCommandAuditLog saveAuditLog(ControlCommandAuditLog auditLog);

    Optional<ControlCommandAuditLog> findAuditLog(UUID auditLogId);

    List<ControlCommandAuditLog> findAuditLogsForCommand(ControlCommandId commandId);

    List<ControlCommandAuditLog> findRecentAuditLogs(int limit);
}
