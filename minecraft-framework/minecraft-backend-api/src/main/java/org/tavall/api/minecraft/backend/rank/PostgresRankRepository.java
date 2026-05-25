package org.tavall.api.minecraft.backend.rank;

import org.tavall.api.minecraft.backend.BackendConnectionProvider;
import org.tavall.api.minecraft.backend.BackendRepositoryJsonCodec;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class PostgresRankRepository implements RankRepository {
    private final BackendConnectionProvider connectionProvider;
    private final BackendRepositoryJsonCodec jsonCodec;

    public PostgresRankRepository(BackendConnectionProvider connectionProvider) {
        this(connectionProvider, new BackendRepositoryJsonCodec());
    }

    public PostgresRankRepository(BackendConnectionProvider connectionProvider, BackendRepositoryJsonCodec jsonCodec) {
        this.connectionProvider = connectionProvider;
        this.jsonCodec = jsonCodec;
    }

    @Override
    public List<RankDefinition> findRankDefinitions() {
        String sql = "SELECT rank_name, power_level, permissions_json, created_at, updated_at "
                + "FROM minecraft_rank_definitions ORDER BY power_level ASC, rank_name ASC";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<RankDefinition> definitions = new ArrayList<>();
            while (resultSet.next()) {
                definitions.add(mapDefinition(resultSet));
            }
            return List.copyOf(definitions);
        } catch (SQLException exception) {
            throw failure("read minecraft rank definitions", exception);
        }
    }

    @Override
    public Optional<RankDefinition> findRankDefinition(String rankName) {
        if (rankName == null || rankName.isBlank()) {
            return Optional.empty();
        }
        String sql = "SELECT rank_name, power_level, permissions_json, created_at, updated_at "
                + "FROM minecraft_rank_definitions WHERE LOWER(rank_name) = LOWER(?)";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, rankName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapDefinition(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw failure("read minecraft rank definition", exception);
        }
    }

    @Override
    public RankDefinition saveRankDefinition(RankDefinition definition) {
        String sql = "INSERT INTO minecraft_rank_definitions "
                + "(rank_name, power_level, permissions_json, created_at, updated_at) "
                + "VALUES (?, ?, ?::jsonb, ?, ?) "
                + "ON CONFLICT (rank_name) DO UPDATE SET "
                + "power_level = EXCLUDED.power_level, "
                + "permissions_json = EXCLUDED.permissions_json, "
                + "updated_at = EXCLUDED.updated_at";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, definition.rankName());
            statement.setInt(2, definition.powerLevel());
            statement.setString(3, jsonCodec.writeStringList(definition.permissions().stream().sorted().toList()));
            statement.setTimestamp(4, Timestamp.from(definition.createdAt()));
            statement.setTimestamp(5, Timestamp.from(definition.updatedAt()));
            statement.executeUpdate();
            return definition;
        } catch (SQLException exception) {
            throw failure("save minecraft rank definition", exception);
        }
    }

    @Override
    public List<RankPlayerProfile> findPlayerProfiles() {
        String sql = "SELECT platform_account_id, display_name, rank_name, power_level, permissions_json, metadata_json, created_at, updated_at "
                + "FROM minecraft_player_rank_profiles ORDER BY LOWER(display_name), platform_account_id";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<RankPlayerProfile> profiles = new ArrayList<>();
            while (resultSet.next()) {
                profiles.add(mapProfile(resultSet));
            }
            return List.copyOf(profiles);
        } catch (SQLException exception) {
            throw failure("read minecraft player rank profiles", exception);
        }
    }

    @Override
    public Optional<RankPlayerProfile> findPlayerProfile(String platformAccountId) {
        if (platformAccountId == null || platformAccountId.isBlank()) {
            return Optional.empty();
        }
        String sql = "SELECT platform_account_id, display_name, rank_name, power_level, permissions_json, metadata_json, created_at, updated_at "
                + "FROM minecraft_player_rank_profiles WHERE platform_account_id = ?";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, platformAccountId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapProfile(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw failure("read minecraft player rank profile", exception);
        }
    }

    @Override
    public Optional<RankPlayerProfile> findPlayerProfileByDisplayName(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            return Optional.empty();
        }
        String sql = "SELECT platform_account_id, display_name, rank_name, power_level, permissions_json, metadata_json, created_at, updated_at "
                + "FROM minecraft_player_rank_profiles WHERE LOWER(display_name) = LOWER(?)";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, displayName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapProfile(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw failure("read minecraft player rank profile by display name", exception);
        }
    }

    @Override
    public RankPlayerProfile savePlayerProfile(RankPlayerProfile profile) {
        String sql = "INSERT INTO minecraft_player_rank_profiles "
                + "(platform_account_id, display_name, rank_name, power_level, permissions_json, metadata_json, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?::jsonb, ?::jsonb, ?, ?) "
                + "ON CONFLICT (platform_account_id) DO UPDATE SET "
                + "display_name = EXCLUDED.display_name, "
                + "rank_name = EXCLUDED.rank_name, "
                + "power_level = EXCLUDED.power_level, "
                + "permissions_json = EXCLUDED.permissions_json, "
                + "metadata_json = EXCLUDED.metadata_json, "
                + "updated_at = EXCLUDED.updated_at";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, profile.platformAccountId());
            statement.setString(2, profile.displayName());
            statement.setString(3, profile.rankName());
            statement.setInt(4, profile.powerLevel());
            statement.setString(5, jsonCodec.writeStringList(profile.permissions().stream().sorted().toList()));
            statement.setString(6, jsonCodec.writeStringMap(profile.metadata()));
            statement.setTimestamp(7, Timestamp.from(profile.createdAt()));
            statement.setTimestamp(8, Timestamp.from(profile.updatedAt()));
            statement.executeUpdate();
            return profile;
        } catch (SQLException exception) {
            throw failure("save minecraft player rank profile", exception);
        }
    }

    private RankDefinition mapDefinition(ResultSet resultSet) throws SQLException {
        return new RankDefinition(
                resultSet.getString("rank_name"),
                resultSet.getInt("power_level"),
                Set.copyOf(jsonCodec.readStringList(resultSet.getString("permissions_json"))),
                toInstant(resultSet, "created_at"),
                toInstant(resultSet, "updated_at")
        );
    }

    private RankPlayerProfile mapProfile(ResultSet resultSet) throws SQLException {
        return new RankPlayerProfile(
                resultSet.getString("platform_account_id"),
                resultSet.getString("display_name"),
                resultSet.getString("rank_name"),
                resultSet.getInt("power_level"),
                Set.copyOf(jsonCodec.readStringList(resultSet.getString("permissions_json"))),
                jsonCodec.readStringMap(resultSet.getString("metadata_json")),
                toInstant(resultSet, "created_at"),
                toInstant(resultSet, "updated_at")
        );
    }

    private Instant toInstant(ResultSet resultSet, String column) throws SQLException {
        Timestamp value = resultSet.getTimestamp(column);
        return value == null ? Instant.EPOCH : value.toInstant();
    }

    private IllegalStateException failure(String action, SQLException exception) {
        return new IllegalStateException("Failed to " + action + ": " + exception.getMessage(), exception);
    }
}
