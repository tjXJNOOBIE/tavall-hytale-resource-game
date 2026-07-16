package org.tavall.control.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.citizen.cache.CitizenAgingConfigCache;
import org.tavall.control.config.CacheConfig;
import org.tavall.control.liveops.config.LiveConfigEntry;
import org.tavall.control.liveops.config.LiveConfigRolloutStrategy;
import org.tavall.control.liveops.config.LiveConfigType;
import org.tavall.control.persistence.PostgresConnectionProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public final class PostgresCitizenAgingConfigRepository implements CitizenAgingConfigRepository, IDependencyInjectableConcrete {
    private final PostgresConnectionProvider connectionProvider;
    private final CitizenAgingConfigCache cache;
    private final ObjectMapper objectMapper;

    public PostgresCitizenAgingConfigRepository(
            PostgresConnectionProvider connectionProvider,
            CitizenAgingConfigCache cache,
            ObjectMapper objectMapper
    ) {
        this.connectionProvider = connectionProvider;
        this.cache = cache;
        this.objectMapper = objectMapper;
    }

    public static PostgresCitizenAgingConfigRepository open(
            PostgresConnectionProvider connectionProvider,
            CacheConfig cacheConfig,
            ObjectMapper objectMapper
    ) {
        return new PostgresCitizenAgingConfigRepository(
                connectionProvider,
                CitizenAgingConfigCache.open(cacheConfig, objectMapper),
                objectMapper
        );
    }

    @Override
    public CitizenAgingConfig current() {
        return cache.read().orElseGet(() -> readFromDatabase()
                .map(config -> {
                    cache.prime(config);
                    return config;
                })
                .orElseGet(() -> save(CitizenAgingConfig.defaults())));
    }

    @Override
    public CitizenAgingConfig save(CitizenAgingConfig config) {
        String valueJson = writeConfig(config);
        Instant now = Instant.now();
        LiveConfigEntry existing = readEntry().orElse(null);
        LiveConfigEntry entry = existing == null
                ? new LiveConfigEntry(
                        UUID.randomUUID(),
                        CitizenAgingConfig.LIVE_CONFIG_KEY,
                        LiveConfigType.BALANCE_VALUE,
                        valueJson,
                        true,
                        CitizenAgingConfig.LIVE_CONFIG_ENVIRONMENT,
                        1L,
                        "citizen-aging-config",
                        now,
                        "Citizen aging configuration.",
                        LiveConfigRolloutStrategy.global()
                )
                : existing.withVersionedValue(valueJson, true, "citizen-aging-config", now);
        upsert(entry);
        cache.write(config);
        return config;
    }

    private Optional<CitizenAgingConfig> readFromDatabase() {
        return readEntry().map(this::decode);
    }

    private Optional<LiveConfigEntry> readEntry() {
        String sql = """
                SELECT config_id, config_key, config_type, value_json::text AS value_json, enabled, environment, version, updated_by, updated_at, description, rollout_strategy_json::text AS rollout_strategy_json
                FROM live_config_entries
                WHERE config_key = ? AND environment = ?
                """;
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, CitizenAgingConfig.LIVE_CONFIG_KEY);
            statement.setString(2, CitizenAgingConfig.LIVE_CONFIG_ENVIRONMENT);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(readLiveConfigEntry(resultSet));
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to read citizen aging configuration.", ex);
        }
    }

    private void upsert(LiveConfigEntry entry) {
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
            throw new IllegalStateException("Failed to save citizen aging configuration.", ex);
        }
    }

    private LiveConfigEntry readLiveConfigEntry(ResultSet resultSet) throws Exception {
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
                resultSet.getString("rollout_strategy_json") == null || resultSet.getString("rollout_strategy_json").isBlank()
                        ? LiveConfigRolloutStrategy.global()
                        : objectMapper.readValue(resultSet.getString("rollout_strategy_json"), LiveConfigRolloutStrategy.class)
        );
    }

    private CitizenAgingConfig decode(LiveConfigEntry entry) {
        try {
            return objectMapper.readValue(entry.valueJson(), CitizenAgingConfig.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse citizen aging configuration.", ex);
        }
    }

    private String writeConfig(CitizenAgingConfig config) {
        try {
            return objectMapper.writeValueAsString(config);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to encode citizen aging configuration.", ex);
        }
    }

    private Instant timestamp(ResultSet resultSet, String column) throws Exception {
        Timestamp timestamp = resultSet.getTimestamp(column);
        return timestamp == null ? Instant.EPOCH : timestamp.toInstant();
    }
}
