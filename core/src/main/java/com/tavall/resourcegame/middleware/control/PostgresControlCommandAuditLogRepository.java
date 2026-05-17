package com.tavall.resourcegame.middleware.control;

import com.tavall.resourcegame.persistence.PostgresConnectionProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class PostgresControlCommandAuditLogRepository implements ControlCommandAuditLogRepository {
    private final PostgresConnectionProvider connectionProvider;
    private final ControlRepositoryJsonCodec jsonCodec;

    public PostgresControlCommandAuditLogRepository(PostgresConnectionProvider connectionProvider) {
        this(connectionProvider, new ControlRepositoryJsonCodec());
    }

    public PostgresControlCommandAuditLogRepository(PostgresConnectionProvider connectionProvider, ControlRepositoryJsonCodec jsonCodec) {
        this.connectionProvider = connectionProvider;
        this.jsonCodec = jsonCodec;
    }

    @Override
    public ControlCommandAuditLog saveAuditLog(ControlCommandAuditLog auditLog) {
        String sql = "INSERT INTO control_command_audit_log "
                + "(audit_log_id, command_id, command_type, issued_by, issued_from, target_scope, target_platforms_json, "
                + "arguments_redacted_json, dry_run, result_state, success, message, created_at, completed_at, metadata_json) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?::jsonb, ?::jsonb, ?, ?, ?, ?, ?, ?, ?::jsonb)";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindAuditLog(statement, auditLog);
            statement.executeUpdate();
            return auditLog;
        } catch (SQLException exception) {
            throw new ControlCommandValidationException("Failed to save control command audit log: " + exception.getMessage());
        }
    }

    @Override
    public Optional<ControlCommandAuditLog> findAuditLog(UUID auditLogId) {
        String sql = selectSql() + " WHERE audit_log_id = ?";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, auditLogId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapAuditLog(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new ControlCommandValidationException("Failed to read control command audit log: " + exception.getMessage());
        }
    }

    @Override
    public List<ControlCommandAuditLog> findAuditLogsForCommand(ControlCommandId commandId) {
        String sql = selectSql() + " WHERE command_id = ? ORDER BY created_at DESC";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, commandId.value());
            return readAuditLogs(statement);
        } catch (SQLException exception) {
            throw new ControlCommandValidationException("Failed to read audit logs for command: " + exception.getMessage());
        }
    }

    @Override
    public List<ControlCommandAuditLog> findRecentAuditLogs(int limit) {
        String sql = selectSql() + " ORDER BY created_at DESC LIMIT ?";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Math.max(0, limit));
            return readAuditLogs(statement);
        } catch (SQLException exception) {
            throw new ControlCommandValidationException("Failed to read recent control command audit logs: " + exception.getMessage());
        }
    }

    private String selectSql() {
        return "SELECT audit_log_id, command_id, command_type, issued_by, issued_from, target_scope, target_platforms_json, "
                + "arguments_redacted_json, dry_run, result_state, success, message, created_at, completed_at, metadata_json "
                + "FROM control_command_audit_log";
    }

    private List<ControlCommandAuditLog> readAuditLogs(PreparedStatement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            java.util.ArrayList<ControlCommandAuditLog> auditLogs = new java.util.ArrayList<>();
            while (resultSet.next()) {
                auditLogs.add(mapAuditLog(resultSet));
            }
            return List.copyOf(auditLogs);
        }
    }

    private void bindAuditLog(PreparedStatement statement, ControlCommandAuditLog auditLog) throws SQLException {
        statement.setObject(1, auditLog.auditLogId());
        statement.setObject(2, auditLog.commandId().value());
        statement.setString(3, auditLog.commandType().name());
        statement.setString(4, auditLog.issuedBy());
        statement.setString(5, auditLog.issuedFrom().name());
        statement.setString(6, auditLog.targetScope().name());
        statement.setString(7, jsonCodec.writePlatforms(auditLog.targetPlatforms()));
        statement.setString(8, jsonCodec.writeStringMap(auditLog.argumentsRedacted()));
        statement.setBoolean(9, auditLog.dryRun());
        statement.setString(10, auditLog.resultState().name());
        statement.setBoolean(11, auditLog.success());
        statement.setString(12, auditLog.message());
        statement.setTimestamp(13, Timestamp.from(auditLog.createdAt()));
        statement.setTimestamp(14, Timestamp.from(auditLog.completedAt()));
        statement.setString(15, jsonCodec.writeStringMap(auditLog.metadata()));
    }

    private ControlCommandAuditLog mapAuditLog(ResultSet resultSet) throws SQLException {
        return new ControlCommandAuditLog(
                (UUID) resultSet.getObject("audit_log_id"),
                new ControlCommandId((UUID) resultSet.getObject("command_id")),
                ControlCommandType.valueOf(resultSet.getString("command_type")),
                resultSet.getString("issued_by"),
                CommandIssuedFrom.valueOf(resultSet.getString("issued_from")),
                CommandTargetScope.valueOf(resultSet.getString("target_scope")),
                jsonCodec.readPlatforms(resultSet.getString("target_platforms_json")),
                jsonCodec.readStringMap(resultSet.getString("arguments_redacted_json")),
                resultSet.getBoolean("dry_run"),
                CommandExecutionState.valueOf(resultSet.getString("result_state")),
                resultSet.getBoolean("success"),
                resultSet.getString("message"),
                toInstant(resultSet, "created_at"),
                toInstant(resultSet, "completed_at"),
                jsonCodec.readStringMap(resultSet.getString("metadata_json"))
        );
    }

    private Instant toInstant(ResultSet resultSet, String column) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp(column);
        return timestamp == null ? Instant.EPOCH : timestamp.toInstant();
    }
}
