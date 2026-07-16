package org.tavall.control.runtime;

import org.tavall.control.identity.UniversalPlayerId;
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

public final class PostgresControlOperatorRepository implements ControlOperatorRepository {
    private final PostgresConnectionProvider connectionProvider;
    private final ControlRepositoryJsonCodec jsonCodec;

    public PostgresControlOperatorRepository(PostgresConnectionProvider connectionProvider) {
        this(connectionProvider, new ControlRepositoryJsonCodec());
    }

    public PostgresControlOperatorRepository(PostgresConnectionProvider connectionProvider, ControlRepositoryJsonCodec jsonCodec) {
        this.connectionProvider = connectionProvider;
        this.jsonCodec = jsonCodec;
    }

    @Override
    public ControlOperator saveOperator(ControlOperator operator) {
        String sql = "INSERT INTO control_operator "
                + "(operator_id, universal_player_id, display_name, role, enabled, created_at, metadata_json) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?::jsonb) "
                + "ON CONFLICT (operator_id) DO UPDATE SET "
                + "universal_player_id = EXCLUDED.universal_player_id, display_name = EXCLUDED.display_name, "
                + "role = EXCLUDED.role, enabled = EXCLUDED.enabled, metadata_json = EXCLUDED.metadata_json";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, operator.operatorId());
            statement.setObject(2, operator.universalPlayerId().map(UniversalPlayerId::value).orElse(null));
            statement.setString(3, operator.displayName());
            statement.setString(4, operator.role().name());
            statement.setBoolean(5, operator.enabled());
            statement.setTimestamp(6, Timestamp.from(operator.createdAt()));
            statement.setString(7, jsonCodec.writeStringMap(operator.metadata()));
            statement.executeUpdate();
            return operator;
        } catch (SQLException exception) {
            throw new ControlCommandValidationException("Failed to save control operator: " + exception.getMessage());
        }
    }

    @Override
    public Optional<ControlOperator> findOperator(UUID operatorId) {
        String sql = selectSql() + " WHERE operator_id = ?";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, operatorId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapOperator(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new ControlCommandValidationException("Failed to read control operator: " + exception.getMessage());
        }
    }

    @Override
    public List<ControlOperator> findOperators() {
        String sql = selectSql() + " ORDER BY role, display_name";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            java.util.ArrayList<ControlOperator> operators = new java.util.ArrayList<>();
            while (resultSet.next()) {
                operators.add(mapOperator(resultSet));
            }
            return List.copyOf(operators);
        } catch (SQLException exception) {
            throw new ControlCommandValidationException("Failed to read control operators: " + exception.getMessage());
        }
    }

    private String selectSql() {
        return "SELECT operator_id, universal_player_id, display_name, role, enabled, created_at, metadata_json FROM control_operator";
    }

    private ControlOperator mapOperator(ResultSet resultSet) throws SQLException {
        UUID playerUuid = (UUID) resultSet.getObject("universal_player_id");
        return new ControlOperator(
                (UUID) resultSet.getObject("operator_id"),
                playerUuid == null ? Optional.empty() : Optional.of(UniversalPlayerId.of(playerUuid)),
                resultSet.getString("display_name"),
                ControlOperatorRole.valueOf(resultSet.getString("role")),
                resultSet.getBoolean("enabled"),
                toInstant(resultSet, "created_at"),
                jsonCodec.readStringMap(resultSet.getString("metadata_json"))
        );
    }

    private Instant toInstant(ResultSet resultSet, String column) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp(column);
        return timestamp == null ? Instant.EPOCH : timestamp.toInstant();
    }
}
