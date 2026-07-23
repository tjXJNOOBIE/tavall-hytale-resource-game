package org.tavall.control.liveops.gui;

import org.tavall.dependency.IDependencyInjectableConcrete;
import org.tavall.control.liveops.LiveOpsDomain;
import org.tavall.control.liveops.config.LiveConfigValidationException;

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

public final class PostgresGlobalGuiRepository implements GlobalGuiRepository, LiveOpsDomain, IDependencyInjectableConcrete {
    @Override
    public Optional<GlobalGuiDefinition> findByKey(String guiKey) {
        String sql = """
                SELECT gui_id, gui_key, title, layout_json::text AS layout_json, version, enabled, updated_at, updated_by
                FROM global_gui_definitions
                WHERE gui_key = ?
                """;
        try (Connection connection = getLiveOpsPostgresConnectionProvider().open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, guiKey);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(readDefinition(resultSet));
            }
        } catch (Exception ex) {
            throw new LiveConfigValidationException("Failed to read global GUI definition.", ex);
        }
    }

    @Override
    public List<GlobalGuiDefinition> findEnabled() {
        String sql = """
                SELECT gui_id, gui_key, title, layout_json::text AS layout_json, version, enabled, updated_at, updated_by
                FROM global_gui_definitions
                WHERE enabled = true
                ORDER BY gui_key
                """;
        try (Connection connection = getLiveOpsPostgresConnectionProvider().open();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<GlobalGuiDefinition> definitions = new ArrayList<>();
            while (resultSet.next()) {
                definitions.add(readDefinition(resultSet));
            }
            return definitions;
        } catch (Exception ex) {
            throw new LiveConfigValidationException("Failed to list global GUI definitions.", ex);
        }
    }

    @Override
    public void save(GlobalGuiDefinition definition) {
        validateLayout(definition.layoutJson());
        String sql = """
                INSERT INTO global_gui_definitions (gui_id, gui_key, title, layout_json, version, enabled, updated_at, updated_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (gui_key) DO UPDATE SET
                    title = EXCLUDED.title,
                    layout_json = EXCLUDED.layout_json,
                    version = EXCLUDED.version,
                    enabled = EXCLUDED.enabled,
                    updated_at = EXCLUDED.updated_at,
                    updated_by = EXCLUDED.updated_by
                """;
        try (Connection connection = getLiveOpsPostgresConnectionProvider().open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, definition.guiId());
            statement.setString(2, definition.guiKey());
            statement.setString(3, definition.title());
            statement.setObject(4, definition.layoutJson(), Types.OTHER);
            statement.setLong(5, definition.version());
            statement.setBoolean(6, definition.enabled());
            statement.setTimestamp(7, Timestamp.from(definition.updatedAt()));
            statement.setString(8, definition.updatedBy());
            statement.executeUpdate();
        } catch (Exception ex) {
            throw new LiveConfigValidationException("Failed to save global GUI definition.", ex);
        }
    }

    private GlobalGuiDefinition readDefinition(ResultSet resultSet) throws Exception {
        return new GlobalGuiDefinition(
                resultSet.getObject("gui_id", UUID.class),
                resultSet.getString("gui_key"),
                resultSet.getString("title"),
                resultSet.getString("layout_json"),
                resultSet.getLong("version"),
                resultSet.getBoolean("enabled"),
                timestamp(resultSet, "updated_at"),
                resultSet.getString("updated_by")
        );
    }

    private Instant timestamp(ResultSet resultSet, String column) throws Exception {
        Timestamp timestamp = resultSet.getTimestamp(column);
        return timestamp == null ? Instant.EPOCH : timestamp.toInstant();
    }

    private void validateLayout(String layoutJson) {
        try {
            getLiveOpsObjectMapper().readTree(layoutJson);
        } catch (Exception ex) {
            throw new LiveConfigValidationException("Global GUI layoutJson must be valid JSON.", ex);
        }
    }
}
