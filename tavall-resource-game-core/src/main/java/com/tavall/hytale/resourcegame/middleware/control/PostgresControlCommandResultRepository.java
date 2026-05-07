package com.tavall.hytale.resourcegame.middleware.control;

import com.tavall.hytale.resourcegame.persistence.PostgresConnectionProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class PostgresControlCommandResultRepository implements ControlCommandResultRepository {
    private final PostgresConnectionProvider connectionProvider;
    private final ControlRepositoryJsonCodec jsonCodec;

    public PostgresControlCommandResultRepository(PostgresConnectionProvider connectionProvider) {
        this(connectionProvider, new ControlRepositoryJsonCodec());
    }

    public PostgresControlCommandResultRepository(PostgresConnectionProvider connectionProvider, ControlRepositoryJsonCodec jsonCodec) {
        this.connectionProvider = connectionProvider;
        this.jsonCodec = jsonCodec;
    }

    @Override
    public ControlCommandResult saveResult(ControlCommandResult result) {
        String sql = "INSERT INTO control_command_result "
                + "(command_id, state, success, message, platform_results_json, changed_object_ids_json, validation_errors_json, started_at, completed_at, metadata_json) "
                + "VALUES (?, ?, ?, ?, ?::jsonb, ?::jsonb, ?::jsonb, ?, ?, ?::jsonb) "
                + "ON CONFLICT (command_id) DO UPDATE SET "
                + "state = EXCLUDED.state, success = EXCLUDED.success, message = EXCLUDED.message, "
                + "platform_results_json = EXCLUDED.platform_results_json, changed_object_ids_json = EXCLUDED.changed_object_ids_json, "
                + "validation_errors_json = EXCLUDED.validation_errors_json, started_at = EXCLUDED.started_at, completed_at = EXCLUDED.completed_at, "
                + "metadata_json = EXCLUDED.metadata_json";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindResult(statement, result);
            statement.executeUpdate();
            return result;
        } catch (SQLException exception) {
            throw new ControlCommandValidationException("Failed to save control command result: " + exception.getMessage());
        }
    }

    @Override
    public Optional<ControlCommandResult> findResult(ControlCommandId commandId) {
        String sql = "SELECT command_id, state, success, message, platform_results_json, changed_object_ids_json, "
                + "validation_errors_json, started_at, completed_at, metadata_json "
                + "FROM control_command_result WHERE command_id = ?";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, commandId.value());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapResult(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new ControlCommandValidationException("Failed to read control command result: " + exception.getMessage());
        }
    }

    @Override
    public List<ControlCommandResult> findRecentResults(int limit) {
        String sql = "SELECT command_id, state, success, message, platform_results_json, changed_object_ids_json, "
                + "validation_errors_json, started_at, completed_at, metadata_json "
                + "FROM control_command_result ORDER BY started_at DESC LIMIT ?";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Math.max(0, limit));
            try (ResultSet resultSet = statement.executeQuery()) {
                java.util.ArrayList<ControlCommandResult> results = new java.util.ArrayList<>();
                while (resultSet.next()) {
                    results.add(mapResult(resultSet));
                }
                return List.copyOf(results);
            }
        } catch (SQLException exception) {
            throw new ControlCommandValidationException("Failed to read recent control command results: " + exception.getMessage());
        }
    }

    private void bindResult(PreparedStatement statement, ControlCommandResult result) throws SQLException {
        statement.setObject(1, result.commandId().value());
        statement.setString(2, result.state().name());
        statement.setBoolean(3, result.success());
        statement.setString(4, result.message());
        statement.setString(5, jsonCodec.writePlatformResults(result.platformResults()));
        statement.setString(6, jsonCodec.writeStringList(result.changedObjectIds()));
        statement.setString(7, jsonCodec.writeStringList(result.validationErrors()));
        statement.setTimestamp(8, Timestamp.from(result.startedAt()));
        statement.setTimestamp(9, Timestamp.from(result.completedAt()));
        statement.setString(10, jsonCodec.writeStringMap(result.metadata()));
    }

    private ControlCommandResult mapResult(ResultSet resultSet) throws SQLException {
        return new ControlCommandResult(
                new ControlCommandId((UUID) resultSet.getObject("command_id")),
                CommandExecutionState.valueOf(resultSet.getString("state")),
                resultSet.getBoolean("success"),
                resultSet.getString("message"),
                jsonCodec.readPlatformResults(resultSet.getString("platform_results_json")),
                jsonCodec.readStringList(resultSet.getString("changed_object_ids_json")),
                jsonCodec.readStringList(resultSet.getString("validation_errors_json")),
                toInstant(resultSet, "started_at"),
                toInstant(resultSet, "completed_at"),
                jsonCodec.readStringMap(resultSet.getString("metadata_json"))
        );
    }

    private Instant toInstant(ResultSet resultSet, String column) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp(column);
        return timestamp == null ? Instant.EPOCH : timestamp.toInstant();
    }
}
