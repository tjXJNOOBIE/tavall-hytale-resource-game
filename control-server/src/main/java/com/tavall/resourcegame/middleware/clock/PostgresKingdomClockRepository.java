package org.tavall.control.clock;

import org.tavall.control.runtime.ControlCommandValidationException;
import org.tavall.control.persistence.ControlPlaneSnapshotJsonCodec;
import org.tavall.control.persistence.PostgresConnectionProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

public final class PostgresKingdomClockRepository implements KingdomClockRepository {
    private final PostgresConnectionProvider connectionProvider;
    private final ControlPlaneSnapshotJsonCodec jsonCodec;

    public PostgresKingdomClockRepository(PostgresConnectionProvider connectionProvider) {
        this(connectionProvider, new ControlPlaneSnapshotJsonCodec());
    }

    public PostgresKingdomClockRepository(PostgresConnectionProvider connectionProvider, ControlPlaneSnapshotJsonCodec jsonCodec) {
        this.connectionProvider = connectionProvider;
        this.jsonCodec = jsonCodec;
    }

    @Override
    public Optional<KingdomClockState> findClockState(String kingdomId) {
        return find("CLOCK_STATE", kingdomId, KingdomClockState.class);
    }

    @Override
    public void saveClockState(KingdomClockState state) {
        save("CLOCK_STATE", state.kingdomId(), state.kingdomId(), state.currentEpochMinute(), state, state.lastTickAt(), state.updatedAt());
    }

    @Override
    public Optional<KingdomClockConfig> findClockConfig(String kingdomId) {
        return find("CLOCK_CONFIG", kingdomId, KingdomClockConfig.class);
    }

    @Override
    public void saveClockConfig(KingdomClockConfig config) {
        String kingdomId = config.kingdomId().orElseThrow(() -> new ControlCommandValidationException("Clock config must be scoped to a kingdom before it can be persisted."));
        save("CLOCK_CONFIG", kingdomId, kingdomId, 0L, config, Instant.now(), Instant.now());
    }

    @Override
    public List<KingdomScheduleRule> findScheduleRules(String kingdomId) {
        String sql = "SELECT payload_json FROM kingdom_clock_object_snapshot "
                + "WHERE object_type = 'SCHEDULE_RULE' AND (kingdom_id IS NULL OR kingdom_id = ?) ORDER BY sort_number DESC, object_id";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, kingdomId);
            return readMany(statement, KingdomScheduleRule.class);
        } catch (SQLException exception) {
            throw failure("read kingdom schedule rules", exception);
        }
    }

    @Override
    public Optional<KingdomScheduleRule> findScheduleRule(String scheduleRuleId) {
        return find("SCHEDULE_RULE", scheduleRuleId, KingdomScheduleRule.class);
    }

    @Override
    public void saveScheduleRule(KingdomScheduleRule rule) {
        save("SCHEDULE_RULE", rule.scheduleRuleId(), rule.kingdomId().orElse(null), rule.priority(), rule, rule.createdAt(), rule.updatedAt());
    }

    @Override
    public Optional<AgingTickPolicy> findAgingTickPolicy(String kingdomId) {
        return find("AGING_TICK_POLICY", kingdomId, AgingTickPolicy.class);
    }

    @Override
    public void saveAgingTickPolicy(AgingTickPolicy policy) {
        String kingdomId = policy.kingdomId().orElseThrow(() -> new ControlCommandValidationException("Aging tick policy must be scoped to a kingdom before it can be persisted."));
        save("AGING_TICK_POLICY", kingdomId, kingdomId, 0L, policy, Instant.now(), Instant.now());
    }

    @Override
    public Optional<Long> findLastAgingTickEpochMinute(String kingdomId) {
        return find("AGING_TICK_STATE", kingdomId, LastAgingTickSnapshot.class).map(LastAgingTickSnapshot::epochMinute);
    }

    @Override
    public void saveLastAgingTickEpochMinute(String kingdomId, long epochMinute) {
        Instant now = Instant.now();
        save("AGING_TICK_STATE", kingdomId, kingdomId, epochMinute, new LastAgingTickSnapshot(kingdomId, epochMinute, now), now, now);
    }

    @Override
    public Set<String> knownKingdomIds() {
        String sql = "SELECT DISTINCT kingdom_id FROM kingdom_clock_object_snapshot WHERE kingdom_id IS NOT NULL ORDER BY kingdom_id";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            TreeSet<String> kingdomIds = new TreeSet<>();
            while (resultSet.next()) {
                kingdomIds.add(resultSet.getString("kingdom_id"));
            }
            return Set.copyOf(kingdomIds);
        } catch (SQLException exception) {
            throw failure("read known kingdom clock ids", exception);
        }
    }

    private <T> Optional<T> find(String objectType, String objectId, Class<T> type) {
        String sql = "SELECT payload_json FROM kingdom_clock_object_snapshot WHERE object_type = ? AND object_id = ?";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, objectType);
            statement.setString(2, objectId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(jsonCodec.read(resultSet.getString("payload_json"), type)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw failure("read kingdom clock snapshot", exception);
        }
    }

    private void save(String objectType, String objectId, String kingdomId, long sortNumber, Object payload, Instant createdAt, Instant updatedAt) {
        String sql = "INSERT INTO kingdom_clock_object_snapshot "
                + "(object_type, object_id, kingdom_id, sort_number, payload_json, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?::jsonb, ?, ?) "
                + "ON CONFLICT (object_type, object_id) DO UPDATE SET "
                + "kingdom_id = EXCLUDED.kingdom_id, sort_number = EXCLUDED.sort_number, payload_json = EXCLUDED.payload_json, updated_at = EXCLUDED.updated_at";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, objectType);
            statement.setString(2, objectId);
            statement.setString(3, kingdomId);
            statement.setLong(4, sortNumber);
            statement.setString(5, jsonCodec.write(payload));
            statement.setTimestamp(6, Timestamp.from(createdAt));
            statement.setTimestamp(7, Timestamp.from(updatedAt));
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw failure("save kingdom clock snapshot", exception);
        }
    }

    private <T> List<T> readMany(PreparedStatement statement, Class<T> type) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            java.util.ArrayList<T> values = new java.util.ArrayList<>();
            while (resultSet.next()) {
                values.add(jsonCodec.read(resultSet.getString("payload_json"), type));
            }
            return List.copyOf(values);
        }
    }

    private ControlCommandValidationException failure(String action, SQLException exception) {
        return new ControlCommandValidationException("Failed to " + action + ": " + exception.getMessage());
    }

    private record LastAgingTickSnapshot(String kingdomId, long epochMinute, Instant updatedAt) {
    }
}
