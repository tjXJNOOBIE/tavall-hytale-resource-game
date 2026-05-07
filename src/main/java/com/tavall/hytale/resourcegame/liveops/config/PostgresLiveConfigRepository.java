package com.tavall.hytale.resourcegame.liveops.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tavall.hytale.resourcegame.persistence.PostgresConnectionProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class PostgresLiveConfigRepository implements LiveConfigRepository {
    private final PostgresConnectionProvider connectionProvider;
    private final ObjectMapper objectMapper;

    public PostgresLiveConfigRepository(PostgresConnectionProvider connectionProvider, ObjectMapper objectMapper) {
        this.connectionProvider = connectionProvider;
        this.objectMapper = objectMapper == null ? new ObjectMapper().findAndRegisterModules() : objectMapper;
    }

    @Override
    public Optional<LiveConfigEntry> findByKey(String key, String environment) {
        String sql = """
                SELECT config_id, config_key, config_type, value_json::text AS value_json, enabled, environment, version, updated_by, updated_at, description, rollout_strategy_json::text AS rollout_strategy_json
                FROM live_config_entries
                WHERE config_key = ? AND environment = ?
                """;
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, key);
            statement.setString(2, environment);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(readEntry(resultSet));
            }
        } catch (Exception ex) {
            throw new LiveConfigValidationException("Failed to read live config entry.", ex);
        }
    }

    @Override
    public List<LiveConfigEntry> findByEnvironment(String environment) {
        String sql = """
                SELECT config_id, config_key, config_type, value_json::text AS value_json, enabled, environment, version, updated_by, updated_at, description, rollout_strategy_json::text AS rollout_strategy_json
                FROM live_config_entries
                WHERE environment = ?
                ORDER BY config_key
                """;
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, environment);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<LiveConfigEntry> entries = new ArrayList<>();
                while (resultSet.next()) {
                    entries.add(readEntry(resultSet));
                }
                return entries;
            }
        } catch (Exception ex) {
            throw new LiveConfigValidationException("Failed to list live config entries.", ex);
        }
    }

    @Override
    public void save(LiveConfigEntry entry) {
        String sql = """
                INSERT INTO live_config_entries (config_id, config_key, config_type, value_json, enabled, environment, version, updated_by, updated_at, description, rollout_strategy_json)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (config_key, environment) DO UPDATE SET
                    config_type = EXCLUDED.config_type,
                    value_json = EXCLUDED.value_json,
                    enabled = EXCLUDED.enabled,
                    version = EXCLUDED.version,
                    updated_by = EXCLUDED.updated_by,
                    updated_at = EXCLUDED.updated_at,
                    description = EXCLUDED.description,
                    rollout_strategy_json = EXCLUDED.rollout_strategy_json
                """;
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, entry.configId());
            statement.setString(2, entry.key());
            statement.setString(3, entry.type().name());
            statement.setObject(4, entry.valueJson(), Types.OTHER);
            statement.setBoolean(5, entry.enabled());
            statement.setString(6, entry.environment());
            statement.setLong(7, entry.version());
            statement.setString(8, entry.updatedBy());
            statement.setTimestamp(9, Timestamp.from(entry.updatedAt()));
            statement.setString(10, entry.description());
            statement.setObject(11, objectMapper.writeValueAsString(entry.rolloutStrategy()), Types.OTHER);
            statement.executeUpdate();
        } catch (Exception ex) {
            throw new LiveConfigValidationException("Failed to save live config entry.", ex);
        }
    }

    private LiveConfigEntry readEntry(ResultSet resultSet) throws Exception {
        return new LiveConfigEntry(
                resultSet.getObject("config_id", UUID.class),
                resultSet.getString("config_key"),
                LiveConfigType.valueOf(resultSet.getString("config_type")),
                resultSet.getString("value_json"),
                resultSet.getBoolean("enabled"),
                resultSet.getString("environment"),
                resultSet.getLong("version"),
                resultSet.getString("updated_by"),
                timestamp(resultSet, "updated_at"),
                resultSet.getString("description"),
                rollout(resultSet.getString("rollout_strategy_json"))
        );
    }

    private Instant timestamp(ResultSet resultSet, String column) throws Exception {
        Timestamp timestamp = resultSet.getTimestamp(column);
        return timestamp == null ? Instant.EPOCH : timestamp.toInstant();
    }

    private LiveConfigRolloutStrategy rollout(String rolloutJson) throws Exception {
        if (rolloutJson == null || rolloutJson.isBlank()) {
            return LiveConfigRolloutStrategy.global();
        }
        return objectMapper.readValue(rolloutJson, LiveConfigRolloutStrategy.class);
    }
}
