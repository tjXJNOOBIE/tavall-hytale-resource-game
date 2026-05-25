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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class PostgresRankRepository implements RankAccess {
    private static final String DEFAULT_RANK_NAME = "Member";

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
        String sql = "SELECT \"RANK\", \"POWERLEVEL\", \"PERMISSIONS\", \"CREATED_AT\", \"UPDATED_AT\" "
                + "FROM ranks ORDER BY \"POWERLEVEL\" ASC, \"RANK\" ASC";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<RankDefinition> definitions = new ArrayList<>();
            while (resultSet.next()) {
                definitions.add(mapDefinition(resultSet));
            }
            return List.copyOf(definitions);
        } catch (SQLException exception) {
            throw failure("read rank definitions", exception);
        }
    }

    @Override
    public Optional<RankDefinition> findRankDefinition(String rankName) {
        if (rankName == null || rankName.isBlank()) {
            return Optional.empty();
        }
        String sql = "SELECT \"RANK\", \"POWERLEVEL\", \"PERMISSIONS\", \"CREATED_AT\", \"UPDATED_AT\" "
                + "FROM ranks WHERE LOWER(\"RANK\") = LOWER(?)";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, rankName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapDefinition(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw failure("read rank definition", exception);
        }
    }

    @Override
    public RankDefinition saveRankDefinition(RankDefinition definition) {
        String sql = "INSERT INTO ranks (\"RANK\", \"POWERLEVEL\", \"PERMISSIONS\", \"CREATED_AT\", \"UPDATED_AT\") "
                + "VALUES (?, ?, ?::jsonb, ?, ?) "
                + "ON CONFLICT (\"RANK\") DO UPDATE SET "
                + "\"POWERLEVEL\" = EXCLUDED.\"POWERLEVEL\", "
                + "\"PERMISSIONS\" = EXCLUDED.\"PERMISSIONS\", "
                + "\"UPDATED_AT\" = EXCLUDED.\"UPDATED_AT\"";
        Instant now = Instant.now();
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, definition.rankName());
            statement.setInt(2, definition.powerLevel());
            statement.setString(3, jsonCodec.writeStringList(definition.permissions().stream().sorted().toList()));
            statement.setTimestamp(4, Timestamp.from(definition.createdAt() == null ? now : definition.createdAt()));
            statement.setTimestamp(5, Timestamp.from(definition.updatedAt() == null ? now : definition.updatedAt()));
            statement.executeUpdate();
            return definition;
        } catch (SQLException exception) {
            throw failure("save rank definition", exception);
        }
    }

    @Override
    public List<RankPlayerProfile> findPlayerProfiles() {
        String sql = "SELECT \"UUID\", \"NAME\", \"RANK\", \"POWERLEVEL\", \"PERMISSIONS\", \"METADATA_JSON\", "
                + "\"CREATED_AT\", \"UPDATED_AT\" FROM player_profile ORDER BY LOWER(\"NAME\"), \"UUID\"";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<RankPlayerProfile> profiles = new ArrayList<>();
            while (resultSet.next()) {
                profiles.add(mapProfile(resultSet));
            }
            return List.copyOf(profiles);
        } catch (SQLException exception) {
            throw failure("read player profiles", exception);
        }
    }

    @Override
    public Optional<RankPlayerProfile> findPlayerProfile(String platformAccountId) {
        if (platformAccountId == null || platformAccountId.isBlank()) {
            return Optional.empty();
        }
        String sql = "SELECT \"UUID\", \"NAME\", \"RANK\", \"POWERLEVEL\", \"PERMISSIONS\", \"METADATA_JSON\", "
                + "\"CREATED_AT\", \"UPDATED_AT\" FROM player_profile WHERE \"UUID\" = ?";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, platformAccountId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapProfile(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw failure("read player profile", exception);
        }
    }

    @Override
    public Optional<RankPlayerProfile> findPlayerProfileByDisplayName(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            return Optional.empty();
        }
        String sql = "SELECT \"UUID\", \"NAME\", \"RANK\", \"POWERLEVEL\", \"PERMISSIONS\", \"METADATA_JSON\", "
                + "\"CREATED_AT\", \"UPDATED_AT\" FROM player_profile WHERE LOWER(\"NAME\") = LOWER(?)";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, displayName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapProfile(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw failure("read player profile by display name", exception);
        }
    }

    @Override
    public RankPlayerProfile savePlayerProfile(RankPlayerProfile profile) {
        String sql = "INSERT INTO player_profile "
                + "(\"UUID\", \"NAME\", \"RANK\", \"POWERLEVEL\", \"PERMISSIONS\", \"METADATA_JSON\", \"CREATED_AT\", \"UPDATED_AT\") "
                + "VALUES (?, ?, ?, ?, ?::jsonb, ?::jsonb, ?, ?) "
                + "ON CONFLICT (\"UUID\") DO UPDATE SET "
                + "\"NAME\" = EXCLUDED.\"NAME\", "
                + "\"RANK\" = EXCLUDED.\"RANK\", "
                + "\"POWERLEVEL\" = EXCLUDED.\"POWERLEVEL\", "
                + "\"PERMISSIONS\" = EXCLUDED.\"PERMISSIONS\", "
                + "\"METADATA_JSON\" = EXCLUDED.\"METADATA_JSON\", "
                + "\"UPDATED_AT\" = EXCLUDED.\"UPDATED_AT\"";
        Instant now = Instant.now();
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, profile.platformAccountId());
            statement.setString(2, profile.displayName());
            statement.setString(3, profile.rankName());
            statement.setInt(4, profile.powerLevel());
            statement.setString(5, jsonCodec.writeStringList(profile.permissions().stream().sorted().toList()));
            statement.setString(6, jsonCodec.writeStringMap(profile.metadata()));
            statement.setTimestamp(7, Timestamp.from(profile.createdAt() == null ? now : profile.createdAt()));
            statement.setTimestamp(8, Timestamp.from(profile.updatedAt() == null ? now : profile.updatedAt()));
            statement.executeUpdate();
            return profile;
        } catch (SQLException exception) {
            throw failure("save player profile", exception);
        }
    }

    @Override
    public void createProfile(UUID playerId, String playerName) {
        String sql = "INSERT INTO player_profile "
                + "(\"UUID\", \"NAME\", \"RANK\", \"POWERLEVEL\", \"PERMISSIONS\", \"METADATA_JSON\", \"CREATED_AT\", \"UPDATED_AT\", \"GRADE\", \"CURRENCY\", \"GLOBALRANK\", \"IP\", \"DEBUGGER\") "
                + "VALUES (?, ?, ?, ?, ?::jsonb, ?::jsonb, NOW(), NOW(), NULL, 0.00, 0.00, '', 0) "
                + "ON CONFLICT (\"UUID\") DO UPDATE SET "
                + "\"NAME\" = EXCLUDED.\"NAME\", "
                + "\"UPDATED_AT\" = EXCLUDED.\"UPDATED_AT\"";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerId.toString());
            statement.setString(2, playerName);
            statement.setString(3, DEFAULT_RANK_NAME);
            statement.setInt(4, 100);
            statement.setString(5, "[]");
            statement.setString(6, "{}");
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw failure("create player profile", exception);
        }
    }

    @Override
    public boolean playerExistsByUsername(String username) {
        return findPlayerProfileByDisplayName(username).isPresent();
    }

    @Override
    public Optional<RankPlayerProfile> findPlayerProfileByUsername(String username) {
        return findPlayerProfileByDisplayName(username);
    }

    @Override
    public void setRankFromUsername(String username, String rank) {
        executeRankUpdate("UPDATE player_profile SET \"RANK\" = ?, \"UPDATED_AT\" = NOW() WHERE LOWER(\"NAME\") = LOWER(?)", rank, username);
    }

    @Override
    public void setRank(UUID uuid, String rank) {
        executeRankUpdate("UPDATE player_profile SET \"RANK\" = ?, \"UPDATED_AT\" = NOW() WHERE \"UUID\" = ?", rank, uuid.toString());
    }

    @Override
    public void revokeRank(UUID uuid, String fallbackRankName) {
        String fallback = fallbackRankName == null || fallbackRankName.isBlank() ? DEFAULT_RANK_NAME : fallbackRankName;
        setRank(uuid, fallback);
    }

    @Override
    public String getRank(UUID uuid) {
        String sql = "SELECT \"RANK\" FROM player_profile WHERE \"UUID\" = ?";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("RANK");
                }
            }
        } catch (SQLException exception) {
            throw failure("get rank", exception);
        }
        return "Couldn't get rank";
    }

    @Override
    public int getPowerLevel(UUID uuid) {
        String sql = "SELECT \"POWERLEVEL\" FROM player_profile WHERE \"UUID\" = ?";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("POWERLEVEL");
                }
            }
        } catch (SQLException exception) {
            throw failure("get power level", exception);
        }
        return 1;
    }

    @Override
    public Set<String> getPermissions(UUID uuid) {
        String sql = "SELECT \"PERMISSIONS\" FROM player_profile WHERE \"UUID\" = ?";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String permissions = resultSet.getString("PERMISSIONS");
                    return jsonCodec.readStringList(permissions).stream().collect(java.util.stream.Collectors.toUnmodifiableSet());
                }
            }
        } catch (SQLException exception) {
            throw failure("get permissions", exception);
        }
        return Set.of();
    }

    @Override
    public void setPermissions(UUID uuid, Set<String> permissions) {
        String sql = "UPDATE player_profile SET \"PERMISSIONS\" = ?::jsonb, \"UPDATED_AT\" = NOW() WHERE \"UUID\" = ?";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, jsonCodec.writeStringList(permissions == null ? List.of() : permissions.stream().sorted().toList()));
            statement.setString(2, uuid.toString());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw failure("set permissions", exception);
        }
    }

    @Override
    public boolean hasPermission(UUID uuid, String permission) {
        return getPermissions(uuid).contains(permission);
    }

    @Override
    public boolean rankExists(String rankName) {
        return findRankDefinition(rankName).isPresent();
    }

    @Override
    public List<String> getRanks() {
        String sql = "SELECT \"RANK\" FROM ranks WHERE \"RANK\" IS NOT NULL ORDER BY \"POWERLEVEL\" ASC, \"RANK\" ASC";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<String> ranks = new ArrayList<>();
            while (resultSet.next()) {
                ranks.add(resultSet.getString("RANK"));
            }
            return List.copyOf(ranks);
        } catch (SQLException exception) {
            throw failure("read rank names", exception);
        }
    }

    @Override
    public String getAllRanks() {
        List<String> ranks = getRanks();
        return ranks.isEmpty() ? "" : ranks.get(ranks.size() - 1);
    }

    private RankDefinition mapDefinition(ResultSet resultSet) throws SQLException {
        return new RankDefinition(
                resultSet.getString("RANK"),
                resultSet.getInt("POWERLEVEL"),
                Set.copyOf(jsonCodec.readStringList(resultSet.getString("PERMISSIONS"))),
                toInstant(resultSet, "CREATED_AT"),
                toInstant(resultSet, "UPDATED_AT")
        );
    }

    private RankPlayerProfile mapProfile(ResultSet resultSet) throws SQLException {
        Map<String, String> metadata = jsonCodec.readStringMap(resultSet.getString("METADATA_JSON"));
        return new RankPlayerProfile(
                resultSet.getString("UUID"),
                resultSet.getString("NAME"),
                resultSet.getString("RANK"),
                resultSet.getInt("POWERLEVEL"),
                Set.copyOf(jsonCodec.readStringList(resultSet.getString("PERMISSIONS"))),
                metadata,
                toInstant(resultSet, "CREATED_AT"),
                toInstant(resultSet, "UPDATED_AT")
        );
    }

    private Instant toInstant(ResultSet resultSet, String column) throws SQLException {
        Timestamp value = resultSet.getTimestamp(column);
        return value == null ? Instant.EPOCH : value.toInstant();
    }

    private void executeRankUpdate(String sql, String rank, String token) {
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, rank);
            statement.setString(2, token);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw failure("update player rank", exception);
        }
    }

    private IllegalStateException failure(String action, SQLException exception) {
        return new IllegalStateException("Failed to " + action + ": " + exception.getMessage(), exception);
    }
}
