package org.tavall.control.runtime;

import org.tavall.control.common.GamePlatform;
import org.tavall.control.persistence.PostgresConnectionProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class PostgresControlPlatformFanoutRetryRepository implements ControlPlatformFanoutRetryRepository {
    private final PostgresConnectionProvider connectionProvider;
    private final ControlRepositoryJsonCodec jsonCodec;

    public PostgresControlPlatformFanoutRetryRepository(PostgresConnectionProvider connectionProvider) {
        this(connectionProvider, new ControlRepositoryJsonCodec());
    }

    public PostgresControlPlatformFanoutRetryRepository(PostgresConnectionProvider connectionProvider, ControlRepositoryJsonCodec jsonCodec) {
        this.connectionProvider = connectionProvider;
        this.jsonCodec = jsonCodec;
    }

    @Override
    public ControlPlatformFanoutRetryRecord saveRetryRecord(ControlPlatformFanoutRetryRecord retryRecord) {
        String sql = "INSERT INTO control_platform_fanout_result "
                + "(fanout_result_id, command_id, command_type, platform, success, message, frontend_event_ids_json, projection_ids_json, "
                + "changed_object_ids_json, retry_state, attempt_count, max_attempts, next_attempt_at, last_attempt_at, metadata_json, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, '[]'::jsonb, '[]'::jsonb, ?::jsonb, ?, ?, ?, ?, ?, ?::jsonb, ?, ?) "
                + "ON CONFLICT (fanout_result_id) DO UPDATE SET "
                + "success = EXCLUDED.success, message = EXCLUDED.message, changed_object_ids_json = EXCLUDED.changed_object_ids_json, "
                + "retry_state = EXCLUDED.retry_state, attempt_count = EXCLUDED.attempt_count, max_attempts = EXCLUDED.max_attempts, "
                + "next_attempt_at = EXCLUDED.next_attempt_at, last_attempt_at = EXCLUDED.last_attempt_at, metadata_json = EXCLUDED.metadata_json, updated_at = EXCLUDED.updated_at";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, retryRecord.retryId());
            statement.setObject(2, retryRecord.commandId().value());
            statement.setString(3, retryRecord.commandType().name());
            statement.setString(4, retryRecord.platform().name());
            statement.setBoolean(5, retryRecord.state() == ControlPlatformFanoutRetryState.SUCCEEDED);
            statement.setString(6, retryRecord.message());
            statement.setString(7, jsonCodec.writeStringList(retryRecord.changedObjectIds()));
            statement.setString(8, retryRecord.state().name());
            statement.setInt(9, retryRecord.attemptCount());
            statement.setInt(10, retryRecord.maxAttempts());
            statement.setTimestamp(11, Timestamp.from(retryRecord.nextAttemptAt()));
            statement.setTimestamp(12, retryRecord.lastAttemptAt().map(Timestamp::from).orElse(null));
            statement.setString(13, jsonCodec.writeStringMap(retryRecord.metadata()));
            statement.setTimestamp(14, Timestamp.from(retryRecord.createdAt()));
            statement.setTimestamp(15, Timestamp.from(retryRecord.updatedAt()));
            statement.executeUpdate();
            return retryRecord;
        } catch (SQLException exception) {
            throw new ControlCommandValidationException("Failed to save platform fanout retry: " + exception.getMessage());
        }
    }

    @Override
    public Optional<ControlPlatformFanoutRetryRecord> findRetryRecord(UUID retryId) {
        String sql = selectSql() + " WHERE fanout_result_id = ?";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, retryId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRetryRecord(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new ControlCommandValidationException("Failed to read platform fanout retry: " + exception.getMessage());
        }
    }

    @Override
    public List<ControlPlatformFanoutRetryRecord> findRetriesForCommand(ControlCommandId commandId) {
        String sql = selectSql() + " WHERE command_id = ? ORDER BY created_at";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, commandId.value());
            return readRetryRecords(statement);
        } catch (SQLException exception) {
            throw new ControlCommandValidationException("Failed to read platform fanout retries for command: " + exception.getMessage());
        }
    }

    @Override
    public List<ControlPlatformFanoutRetryRecord> findDueRetries(Instant now, int limit) {
        String sql = selectSql() + " WHERE retry_state IN ('PENDING', 'RETRYING') AND next_attempt_at <= ? ORDER BY next_attempt_at LIMIT ?";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.from(now));
            statement.setInt(2, Math.max(0, limit));
            return readRetryRecords(statement);
        } catch (SQLException exception) {
            throw new ControlCommandValidationException("Failed to read due platform fanout retries: " + exception.getMessage());
        }
    }

    private String selectSql() {
        return "SELECT fanout_result_id, command_id, command_type, platform, changed_object_ids_json, retry_state, attempt_count, "
                + "max_attempts, next_attempt_at, last_attempt_at, message, created_at, updated_at, metadata_json FROM control_platform_fanout_result";
    }

    private List<ControlPlatformFanoutRetryRecord> readRetryRecords(PreparedStatement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            java.util.ArrayList<ControlPlatformFanoutRetryRecord> retryRecords = new java.util.ArrayList<>();
            while (resultSet.next()) {
                retryRecords.add(mapRetryRecord(resultSet));
            }
            return List.copyOf(retryRecords);
        }
    }

    private ControlPlatformFanoutRetryRecord mapRetryRecord(ResultSet resultSet) throws SQLException {
        Timestamp lastAttempt = resultSet.getTimestamp("last_attempt_at");
        return new ControlPlatformFanoutRetryRecord(
                (UUID) resultSet.getObject("fanout_result_id"),
                new ControlCommandId((UUID) resultSet.getObject("command_id")),
                ControlCommandType.valueOf(resultSet.getString("command_type")),
                GamePlatform.valueOf(resultSet.getString("platform")),
                jsonCodec.readStringList(resultSet.getString("changed_object_ids_json")),
                resultSet.getInt("attempt_count"),
                resultSet.getInt("max_attempts"),
                toInstant(resultSet, "next_attempt_at"),
                lastAttempt == null ? Optional.empty() : Optional.of(lastAttempt.toInstant()),
                ControlPlatformFanoutRetryState.valueOf(resultSet.getString("retry_state")),
                resultSet.getString("message"),
                toInstant(resultSet, "created_at"),
                toInstant(resultSet, "updated_at"),
                jsonCodec.readStringMap(resultSet.getString("metadata_json"))
        );
    }

    private Instant toInstant(ResultSet resultSet, String column) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp(column);
        return timestamp == null ? Instant.EPOCH : timestamp.toInstant();
    }
}
