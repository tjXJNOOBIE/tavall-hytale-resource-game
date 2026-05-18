package org.tavall.control.runtime;

import org.tavall.control.persistence.PostgresConnectionProvider;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class PostgresScheduledControlCommandRepository implements ScheduledControlCommandRepository {
    private final PostgresConnectionProvider connectionProvider;
    private final ControlRepositoryJsonCodec jsonCodec;

    public PostgresScheduledControlCommandRepository(PostgresConnectionProvider connectionProvider) {
        this(connectionProvider, new ControlRepositoryJsonCodec());
    }

    public PostgresScheduledControlCommandRepository(PostgresConnectionProvider connectionProvider, ControlRepositoryJsonCodec jsonCodec) {
        this.connectionProvider = connectionProvider;
        this.jsonCodec = jsonCodec;
    }

    @Override
    public ScheduledControlCommand saveScheduledCommand(ScheduledControlCommand scheduledCommand) {
        String sql = "INSERT INTO control_command_schedule "
                + "(schedule_id, command_id, command_type, issued_by, operator_role, issued_from, target_scope, target_platforms_json, "
                + "arguments_json, dry_run, run_at, state, created_at, dispatched_at, metadata_json) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?::jsonb, ?, ?, ?, ?, ?, ?::jsonb) "
                + "ON CONFLICT (schedule_id) DO UPDATE SET "
                + "run_at = EXCLUDED.run_at, state = EXCLUDED.state, dispatched_at = EXCLUDED.dispatched_at, metadata_json = EXCLUDED.metadata_json";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindScheduledCommand(statement, scheduledCommand);
            statement.executeUpdate();
            return scheduledCommand;
        } catch (SQLException exception) {
            throw new ControlCommandValidationException("Failed to save scheduled control command: " + exception.getMessage());
        }
    }

    @Override
    public Optional<ScheduledControlCommand> findScheduledCommand(UUID scheduleId) {
        String sql = selectSql() + " WHERE schedule_id = ?";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, scheduleId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapScheduledCommand(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new ControlCommandValidationException("Failed to read scheduled control command: " + exception.getMessage());
        }
    }

    @Override
    public List<ScheduledControlCommand> findDueCommands(Instant now, int limit) {
        String sql = selectSql() + " WHERE state = 'PENDING' AND run_at <= ? ORDER BY run_at LIMIT ?";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.from(now));
            statement.setInt(2, Math.max(0, limit));
            try (ResultSet resultSet = statement.executeQuery()) {
                java.util.ArrayList<ScheduledControlCommand> scheduledCommands = new java.util.ArrayList<>();
                while (resultSet.next()) {
                    scheduledCommands.add(mapScheduledCommand(resultSet));
                }
                return List.copyOf(scheduledCommands);
            }
        } catch (SQLException exception) {
            throw new ControlCommandValidationException("Failed to read due scheduled control commands: " + exception.getMessage());
        }
    }

    private String selectSql() {
        return "SELECT schedule_id, command_id, command_type, issued_by, operator_role, issued_from, target_scope, target_platforms_json, "
                + "arguments_json, dry_run, run_at, state, created_at, dispatched_at, metadata_json FROM control_command_schedule";
    }

    private void bindScheduledCommand(PreparedStatement statement, ScheduledControlCommand scheduledCommand) throws SQLException {
        ControlCommand command = scheduledCommand.command();
        statement.setObject(1, scheduledCommand.scheduleId());
        statement.setObject(2, command.commandId().value());
        statement.setString(3, command.commandType().name());
        statement.setString(4, command.issuedBy().displayName());
        statement.setString(5, command.issuedBy().role().name());
        statement.setString(6, command.issuedFrom().name());
        statement.setString(7, command.targetScope().name());
        statement.setString(8, jsonCodec.writePlatforms(command.targetPlatforms()));
        statement.setString(9, jsonCodec.writeStringMap(command.arguments()));
        statement.setBoolean(10, command.dryRun());
        statement.setTimestamp(11, Timestamp.from(scheduledCommand.runAt()));
        statement.setString(12, scheduledCommand.state().name());
        statement.setTimestamp(13, Timestamp.from(scheduledCommand.createdAt()));
        statement.setTimestamp(14, scheduledCommand.dispatchedAt().map(Timestamp::from).orElse(null));
        statement.setString(15, jsonCodec.writeStringMap(scheduledCommand.metadata()));
    }

    private ScheduledControlCommand mapScheduledCommand(ResultSet resultSet) throws SQLException {
        String issuedBy = resultSet.getString("issued_by");
        ControlOperatorRole role = ControlOperatorRole.valueOf(resultSet.getString("operator_role"));
        Instant createdAt = toInstant(resultSet, "created_at");
        ControlOperator operator = new ControlOperator(
                UUID.nameUUIDFromBytes(("scheduled-control-operator:" + issuedBy + ":" + role.name()).getBytes(StandardCharsets.UTF_8)),
                Optional.empty(),
                issuedBy,
                role,
                true,
                createdAt,
                Map.of("scheduledReplay", "true")
        );
        ControlCommand command = new ControlCommand(
                new ControlCommandId((UUID) resultSet.getObject("command_id")),
                ControlCommandType.valueOf(resultSet.getString("command_type")),
                operator,
                CommandIssuedFrom.valueOf(resultSet.getString("issued_from")),
                CommandTargetScope.valueOf(resultSet.getString("target_scope")),
                jsonCodec.readPlatforms(resultSet.getString("target_platforms_json")),
                jsonCodec.readStringMap(resultSet.getString("arguments_json")),
                resultSet.getBoolean("dry_run"),
                createdAt,
                Map.of("scheduledReplay", "true")
        );
        Timestamp dispatchedAt = resultSet.getTimestamp("dispatched_at");
        return new ScheduledControlCommand(
                (UUID) resultSet.getObject("schedule_id"),
                command,
                toInstant(resultSet, "run_at"),
                ScheduledControlCommandState.valueOf(resultSet.getString("state")),
                createdAt,
                dispatchedAt == null ? Optional.empty() : Optional.of(dispatchedAt.toInstant()),
                jsonCodec.readStringMap(resultSet.getString("metadata_json"))
        );
    }

    private Instant toInstant(ResultSet resultSet, String column) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp(column);
        return timestamp == null ? Instant.EPOCH : timestamp.toInstant();
    }
}
