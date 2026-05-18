package org.tavall.control.persistence;

import org.tavall.control.companion.CompanionBaseAttributes;
import org.tavall.control.companion.CompanionBehaviorState;
import org.tavall.control.companion.CompanionData;
import org.tavall.control.companion.CompanionMoraleState;
import org.tavall.control.companion.CompanionRepository;
import org.tavall.control.companion.CompanionSkillSlot;
import org.tavall.control.companion.CompanionStats;
import org.tavall.control.companion.CompanionStatus;
import org.tavall.control.companion.CompanionTrainingSession;
import org.tavall.control.companion.CompanionType;
import org.tavall.control.companion.CompanionWallAssignment;
import org.tavall.control.companion.CompanionWisdomUpgrade;
import org.tavall.control.runtime.ControlCommandValidationException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class PostgresCompanionRepository implements CompanionRepository {
    private final PostgresConnectionProvider connectionProvider;
    private final ControlPlaneSnapshotJsonCodec jsonCodec;

    public PostgresCompanionRepository(PostgresConnectionProvider connectionProvider) {
        this(connectionProvider, new ControlPlaneSnapshotJsonCodec());
    }

    public PostgresCompanionRepository(PostgresConnectionProvider connectionProvider, ControlPlaneSnapshotJsonCodec jsonCodec) {
        this.connectionProvider = connectionProvider;
        this.jsonCodec = jsonCodec;
    }

    @Override
    public CompanionData saveCompanion(CompanionData companion) {
        String sql = """
                INSERT INTO companions (
                    companion_id, player_id, type, status, behavior_state, morale_state, level, xp, active_skin_id,
                    active_wall_section_id, skill_slots_json, calculated_stats_json, metadata_json, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?::jsonb, ?::jsonb, ?, ?)
                ON CONFLICT (companion_id) DO UPDATE SET
                    player_id = EXCLUDED.player_id,
                    type = EXCLUDED.type,
                    status = EXCLUDED.status,
                    behavior_state = EXCLUDED.behavior_state,
                    morale_state = EXCLUDED.morale_state,
                    level = EXCLUDED.level,
                    xp = EXCLUDED.xp,
                    active_skin_id = EXCLUDED.active_skin_id,
                    active_wall_section_id = EXCLUDED.active_wall_section_id,
                    skill_slots_json = EXCLUDED.skill_slots_json,
                    calculated_stats_json = EXCLUDED.calculated_stats_json,
                    metadata_json = EXCLUDED.metadata_json,
                    updated_at = EXCLUDED.updated_at
                """;
        String attributesSql = """
                INSERT INTO companion_attributes (companion_id, intel, strength, agility)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (companion_id) DO UPDATE SET
                    intel = EXCLUDED.intel,
                    strength = EXCLUDED.strength,
                    agility = EXCLUDED.agility
                """;
        try (Connection connection = connectionProvider.open()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setObject(1, companion.companionId());
                statement.setObject(2, companion.ownerPlayerId());
                statement.setString(3, companion.type().name());
                statement.setString(4, companion.status().name());
                statement.setString(5, companion.behaviorState().name());
                statement.setString(6, companion.moraleState().name());
                statement.setInt(7, companion.level());
                statement.setLong(8, companion.xp());
                statement.setObject(9, companion.activeSkinId().orElse(null));
                statement.setString(10, companion.activeWallSectionId().orElse(null));
                statement.setString(11, jsonCodec.write(companion.skillSlots()));
                statement.setString(12, jsonCodec.write(companion.calculatedStats()));
                statement.setString(13, jsonCodec.write(companion.metadata()));
                statement.setLong(14, companion.createdAtEpochMillis());
                statement.setLong(15, companion.updatedAtEpochMillis());
                statement.executeUpdate();
            }
            try (PreparedStatement statement = connection.prepareStatement(attributesSql)) {
                statement.setObject(1, companion.companionId());
                statement.setDouble(2, companion.baseAttributes().intel());
                statement.setDouble(3, companion.baseAttributes().strength());
                statement.setDouble(4, companion.baseAttributes().agility());
                statement.executeUpdate();
            }
            return companion;
        } catch (SQLException exception) {
            throw failure("save companion", exception);
        }
    }

    @Override
    public Optional<CompanionData> findCompanion(UUID companionId) {
        return readCompanions("WHERE c.companion_id = ?", companionId).stream().findFirst();
    }

    @Override
    public List<CompanionData> findCompanionsForPlayer(UUID ownerPlayerId) {
        return readCompanions("WHERE c.player_id = ? ORDER BY c.created_at, c.companion_id", ownerPlayerId);
    }

    @Override
    public Optional<CompanionData> findActiveCompanion(UUID ownerPlayerId) {
        return readCompanions("WHERE c.player_id = ? ORDER BY c.created_at, c.companion_id LIMIT 1", ownerPlayerId).stream().findFirst();
    }

    @Override
    public CompanionTrainingSession saveTrainingSession(CompanionTrainingSession trainingSession) {
        String sql = """
                INSERT INTO companion_training_sessions
                (training_session_id, companion_id, player_id, started_at, expected_completed_at, expected_xp, claimed, metadata_json)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?::jsonb)
                ON CONFLICT (training_session_id) DO UPDATE SET
                    expected_completed_at = EXCLUDED.expected_completed_at,
                    expected_xp = EXCLUDED.expected_xp,
                    claimed = EXCLUDED.claimed,
                    metadata_json = EXCLUDED.metadata_json
                """;
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, trainingSession.trainingSessionId());
            statement.setObject(2, trainingSession.companionId());
            statement.setObject(3, trainingSession.ownerPlayerId());
            statement.setLong(4, trainingSession.startedAtEpochMillis());
            statement.setLong(5, trainingSession.expectedCompletedAtEpochMillis());
            statement.setLong(6, trainingSession.expectedXp());
            statement.setBoolean(7, trainingSession.claimed());
            statement.setString(8, jsonCodec.write(trainingSession.metadata()));
            statement.executeUpdate();
            return trainingSession;
        } catch (SQLException exception) {
            throw failure("save companion training session", exception);
        }
    }

    @Override
    public Optional<CompanionTrainingSession> findActiveTrainingSession(UUID companionId) {
        String sql = "SELECT * FROM companion_training_sessions WHERE companion_id = ? AND claimed = false ORDER BY started_at DESC LIMIT 1";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, companionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapTrainingSession(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw failure("read companion training session", exception);
        }
    }

    @Override
    public void deleteTrainingSession(UUID trainingSessionId) {
        deleteById("DELETE FROM companion_training_sessions WHERE training_session_id = ?", trainingSessionId);
    }

    @Override
    public CompanionWisdomUpgrade saveWisdomUpgrade(CompanionWisdomUpgrade wisdomUpgrade) {
        String sql = """
                INSERT INTO companion_skill_upgrades
                (companion_id, skill_id, skill_level, cooldown_modifier, power_modifier, updated_at, metadata_json)
                VALUES (?, ?, ?, ?, ?, ?, ?::jsonb)
                ON CONFLICT (companion_id, skill_id) DO UPDATE SET
                    skill_level = EXCLUDED.skill_level,
                    cooldown_modifier = EXCLUDED.cooldown_modifier,
                    power_modifier = EXCLUDED.power_modifier,
                    updated_at = EXCLUDED.updated_at,
                    metadata_json = EXCLUDED.metadata_json
                """;
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, wisdomUpgrade.companionId());
            statement.setObject(2, wisdomUpgrade.skillId());
            statement.setInt(3, wisdomUpgrade.skillLevel());
            statement.setDouble(4, wisdomUpgrade.cooldownModifier());
            statement.setDouble(5, wisdomUpgrade.powerModifier());
            statement.setLong(6, wisdomUpgrade.updatedAtEpochMillis());
            statement.setString(7, jsonCodec.write(wisdomUpgrade.metadata()));
            statement.executeUpdate();
            return wisdomUpgrade;
        } catch (SQLException exception) {
            throw failure("save companion wisdom upgrade", exception);
        }
    }

    @Override
    public Optional<CompanionWisdomUpgrade> findWisdomUpgrade(UUID companionId, UUID skillId) {
        return readWisdomUpgrades("WHERE companion_id = ? AND skill_id = ?", companionId, skillId).stream().findFirst();
    }

    @Override
    public List<CompanionWisdomUpgrade> findWisdomUpgrades(UUID companionId) {
        return readWisdomUpgrades("WHERE companion_id = ? ORDER BY skill_id", companionId);
    }

    @Override
    public CompanionWallAssignment saveWallAssignment(CompanionWallAssignment wallAssignment) {
        String sql = """
                INSERT INTO companion_wall_assignments
                (companion_id, player_id, wall_section_id, defense_bonus, updated_at, metadata_json)
                VALUES (?, ?, ?, ?, ?, ?::jsonb)
                ON CONFLICT (companion_id) DO UPDATE SET
                    player_id = EXCLUDED.player_id,
                    wall_section_id = EXCLUDED.wall_section_id,
                    defense_bonus = EXCLUDED.defense_bonus,
                    updated_at = EXCLUDED.updated_at,
                    metadata_json = EXCLUDED.metadata_json
                """;
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, wallAssignment.companionId());
            statement.setObject(2, wallAssignment.ownerPlayerId());
            statement.setString(3, wallAssignment.wallSectionId());
            statement.setDouble(4, wallAssignment.defenseBonus());
            statement.setLong(5, wallAssignment.updatedAtEpochMillis());
            statement.setString(6, jsonCodec.write(wallAssignment.metadata()));
            statement.executeUpdate();
            return wallAssignment;
        } catch (SQLException exception) {
            throw failure("save companion wall assignment", exception);
        }
    }

    @Override
    public Optional<CompanionWallAssignment> findWallAssignment(UUID companionId) {
        return readWallAssignments("WHERE companion_id = ?", companionId).stream().findFirst();
    }

    @Override
    public List<CompanionWallAssignment> findWallAssignmentsForPlayer(UUID ownerPlayerId) {
        return readWallAssignments("WHERE player_id = ? ORDER BY wall_section_id", ownerPlayerId);
    }

    @Override
    public void deleteWallAssignment(UUID companionId) {
        deleteById("DELETE FROM companion_wall_assignments WHERE companion_id = ?", companionId);
    }

    private List<CompanionData> readCompanions(String condition, Object... values) {
        String sql = "SELECT c.*, a.intel, a.strength, a.agility FROM companions c JOIN companion_attributes a ON a.companion_id = c.companion_id " + condition;
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, values);
            try (ResultSet resultSet = statement.executeQuery()) {
                java.util.ArrayList<CompanionData> companions = new java.util.ArrayList<>();
                while (resultSet.next()) {
                    companions.add(mapCompanion(resultSet));
                }
                return List.copyOf(companions);
            }
        } catch (SQLException exception) {
            throw failure("read companions", exception);
        }
    }

    private List<CompanionWisdomUpgrade> readWisdomUpgrades(String condition, Object... values) {
        String sql = "SELECT * FROM companion_skill_upgrades " + condition;
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, values);
            try (ResultSet resultSet = statement.executeQuery()) {
                java.util.ArrayList<CompanionWisdomUpgrade> upgrades = new java.util.ArrayList<>();
                while (resultSet.next()) {
                    upgrades.add(new CompanionWisdomUpgrade(
                            (UUID) resultSet.getObject("companion_id"),
                            (UUID) resultSet.getObject("skill_id"),
                            resultSet.getInt("skill_level"),
                            resultSet.getDouble("cooldown_modifier"),
                            resultSet.getDouble("power_modifier"),
                            resultSet.getLong("updated_at"),
                            readMap(resultSet.getString("metadata_json"))
                    ));
                }
                return List.copyOf(upgrades);
            }
        } catch (SQLException exception) {
            throw failure("read companion wisdom upgrades", exception);
        }
    }

    private List<CompanionWallAssignment> readWallAssignments(String condition, Object... values) {
        String sql = "SELECT * FROM companion_wall_assignments " + condition;
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, values);
            try (ResultSet resultSet = statement.executeQuery()) {
                java.util.ArrayList<CompanionWallAssignment> assignments = new java.util.ArrayList<>();
                while (resultSet.next()) {
                    assignments.add(new CompanionWallAssignment(
                            (UUID) resultSet.getObject("player_id"),
                            (UUID) resultSet.getObject("companion_id"),
                            resultSet.getString("wall_section_id"),
                            resultSet.getDouble("defense_bonus"),
                            resultSet.getLong("updated_at"),
                            readMap(resultSet.getString("metadata_json"))
                    ));
                }
                return List.copyOf(assignments);
            }
        } catch (SQLException exception) {
            throw failure("read companion wall assignments", exception);
        }
    }

    private CompanionData mapCompanion(ResultSet resultSet) throws SQLException {
        return new CompanionData(
                (UUID) resultSet.getObject("companion_id"),
                (UUID) resultSet.getObject("player_id"),
                CompanionType.valueOf(resultSet.getString("type")),
                CompanionStatus.valueOf(resultSet.getString("status")),
                CompanionBehaviorState.valueOf(resultSet.getString("behavior_state")),
                CompanionMoraleState.valueOf(resultSet.getString("morale_state")),
                resultSet.getInt("level"),
                resultSet.getLong("xp"),
                resultSet.getLong("created_at"),
                resultSet.getLong("updated_at"),
                new CompanionBaseAttributes(resultSet.getDouble("intel"), resultSet.getDouble("strength"), resultSet.getDouble("agility")),
                jsonCodec.read(resultSet.getString("calculated_stats_json"), CompanionStats.class),
                readSkillSlots(resultSet.getString("skill_slots_json")),
                Optional.ofNullable((UUID) resultSet.getObject("active_skin_id")),
                Optional.ofNullable(resultSet.getString("active_wall_section_id")),
                readMap(resultSet.getString("metadata_json"))
        );
    }

    private CompanionTrainingSession mapTrainingSession(ResultSet resultSet) throws SQLException {
        return new CompanionTrainingSession(
                (UUID) resultSet.getObject("training_session_id"),
                (UUID) resultSet.getObject("companion_id"),
                (UUID) resultSet.getObject("player_id"),
                resultSet.getLong("started_at"),
                resultSet.getLong("expected_completed_at"),
                resultSet.getLong("expected_xp"),
                resultSet.getBoolean("claimed"),
                readMap(resultSet.getString("metadata_json"))
        );
    }

    private List<CompanionSkillSlot> readSkillSlots(String json) {
        return List.copyOf(Arrays.asList(jsonCodec.read(json, CompanionSkillSlot[].class)));
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> readMap(String json) {
        return jsonCodec.read(json, Map.class);
    }

    private void deleteById(String sql, UUID id) {
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw failure("delete companion record", exception);
        }
    }

    private void bind(PreparedStatement statement, Object... values) throws SQLException {
        for (int index = 0; index < values.length; index++) {
            statement.setObject(index + 1, values[index]);
        }
    }

    private ControlCommandValidationException failure(String action, SQLException exception) {
        return new ControlCommandValidationException("Failed to " + action + ": " + exception.getMessage());
    }
}
