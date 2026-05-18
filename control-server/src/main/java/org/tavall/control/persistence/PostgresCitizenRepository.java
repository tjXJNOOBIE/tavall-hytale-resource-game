package org.tavall.control.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.tavall.control.domain.CitizenJobType;
import org.tavall.control.citizen.CitizenAgeStage;
import org.tavall.control.citizen.CitizenData;
import org.tavall.control.citizen.CitizenHealthState;
import org.tavall.control.citizen.CitizenHousingState;
import org.tavall.control.citizen.CitizenId;
import org.tavall.control.citizen.CitizenMoraleState;
import org.tavall.control.citizen.CitizenNutritionState;
import org.tavall.control.citizen.CitizenRepository;
import org.tavall.control.citizen.CitizenStatBlock;
import org.tavall.control.citizen.CitizenStatus;
import org.tavall.control.citizen.CitizenTrainingState;
import org.tavall.control.citizen.CitizenTroopLinkState;
import org.tavall.control.identity.UniversalPlayerId;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class PostgresCitizenRepository implements CitizenRepository {
    private final PostgresConnectionProvider connectionProvider;
    private final ObjectMapper objectMapper;

    public PostgresCitizenRepository(PostgresConnectionProvider connectionProvider, ObjectMapper objectMapper) {
        this.connectionProvider = connectionProvider;
        this.objectMapper = objectMapper;
    }

    @Override
    public CitizenData saveCitizen(CitizenData citizen) {
        String sql = """
                INSERT INTO citizen_records (
                    citizen_id, owner_player_id, kingdom_id, display_name, born_at_epoch_millis, age_stage, status, job_type,
                    health_state, morale_state, housing_state, nutrition_state, training_state, troop_link_state,
                    strength, endurance, agility, discipline, intelligence, morale_resilience, work_efficiency, combat_potential,
                    created_at_epoch_millis, updated_at_epoch_millis, last_age_stage_processed_at_epoch_millis, metadata_json
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (citizen_id) DO UPDATE SET
                    owner_player_id = EXCLUDED.owner_player_id,
                    kingdom_id = EXCLUDED.kingdom_id,
                    display_name = EXCLUDED.display_name,
                    born_at_epoch_millis = EXCLUDED.born_at_epoch_millis,
                    age_stage = EXCLUDED.age_stage,
                    status = EXCLUDED.status,
                    job_type = EXCLUDED.job_type,
                    health_state = EXCLUDED.health_state,
                    morale_state = EXCLUDED.morale_state,
                    housing_state = EXCLUDED.housing_state,
                    nutrition_state = EXCLUDED.nutrition_state,
                    training_state = EXCLUDED.training_state,
                    troop_link_state = EXCLUDED.troop_link_state,
                    strength = EXCLUDED.strength,
                    endurance = EXCLUDED.endurance,
                    agility = EXCLUDED.agility,
                    discipline = EXCLUDED.discipline,
                    intelligence = EXCLUDED.intelligence,
                    morale_resilience = EXCLUDED.morale_resilience,
                    work_efficiency = EXCLUDED.work_efficiency,
                    combat_potential = EXCLUDED.combat_potential,
                    updated_at_epoch_millis = EXCLUDED.updated_at_epoch_millis,
                    last_age_stage_processed_at_epoch_millis = EXCLUDED.last_age_stage_processed_at_epoch_millis,
                    metadata_json = EXCLUDED.metadata_json
                RETURNING *
                """;
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindCitizen(statement, citizen);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return mapRow(resultSet);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to save citizen.", exception);
        }
    }

    @Override
    public Optional<CitizenData> findCitizen(CitizenId citizenId) {
        return queryOne("SELECT * FROM citizen_records WHERE citizen_id = ?", statement -> statement.setObject(1, citizenId.value()));
    }

    @Override
    public List<CitizenData> findCitizensForPlayer(UniversalPlayerId ownerPlayerId) {
        return queryMany("SELECT * FROM citizen_records WHERE owner_player_id = ? ORDER BY created_at_epoch_millis, citizen_id", statement -> statement.setObject(1, ownerPlayerId.value()));
    }

    @Override
    public List<CitizenData> findCitizensForKingdom(String kingdomId) {
        return queryMany("SELECT * FROM citizen_records WHERE kingdom_id = ? ORDER BY created_at_epoch_millis, citizen_id", statement -> statement.setString(1, kingdomId));
    }

    @Override
    public List<CitizenData> findAllCitizens() {
        return queryMany("SELECT * FROM citizen_records ORDER BY created_at_epoch_millis, citizen_id", statement -> {
        });
    }

    private Optional<CitizenData> queryOne(String sql, SqlStatementBinder binder) {
        List<CitizenData> results = queryMany(sql, binder);
        return results.stream().findFirst();
    }

    private List<CitizenData> queryMany(String sql, SqlStatementBinder binder) {
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.bind(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                java.util.ArrayList<CitizenData> citizens = new java.util.ArrayList<>();
                while (resultSet.next()) {
                    citizens.add(mapRow(resultSet));
                }
                return List.copyOf(citizens);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to query citizens.", exception);
        }
    }

    private void bindCitizen(PreparedStatement statement, CitizenData citizen) throws SQLException {
        statement.setObject(1, citizen.citizenId().value());
        statement.setObject(2, citizen.ownerPlayerId().value());
        statement.setString(3, citizen.kingdomId());
        statement.setString(4, citizen.displayName());
        statement.setLong(5, citizen.bornAtEpochMillis());
        statement.setString(6, citizen.ageStage().name());
        statement.setString(7, citizen.status().name());
        statement.setString(8, citizen.jobType().name());
        statement.setString(9, citizen.healthState().name());
        statement.setString(10, citizen.moraleState().name());
        statement.setString(11, citizen.housingState().name());
        statement.setString(12, citizen.nutritionState().name());
        statement.setString(13, citizen.trainingState().name());
        statement.setString(14, citizen.troopLinkState().name());
        statement.setInt(15, citizen.statBlock().strength());
        statement.setInt(16, citizen.statBlock().endurance());
        statement.setInt(17, citizen.statBlock().agility());
        statement.setInt(18, citizen.statBlock().discipline());
        statement.setInt(19, citizen.statBlock().intelligence());
        statement.setInt(20, citizen.statBlock().moraleResilience());
        statement.setInt(21, citizen.statBlock().workEfficiency());
        statement.setInt(22, citizen.statBlock().combatPotential());
        statement.setLong(23, citizen.createdAtEpochMillis());
        statement.setLong(24, citizen.updatedAtEpochMillis());
        statement.setLong(25, citizen.lastAgeStageProcessedAtEpochMillis());
        statement.setString(26, writeMetadata(citizen.metadata()));
    }

    private CitizenData mapRow(ResultSet resultSet) throws SQLException {
        return new CitizenData(
                new CitizenId((UUID) resultSet.getObject("citizen_id")),
                UniversalPlayerId.of((UUID) resultSet.getObject("owner_player_id")),
                resultSet.getString("kingdom_id"),
                resultSet.getString("display_name"),
                resultSet.getLong("born_at_epoch_millis"),
                CitizenAgeStage.valueOf(resultSet.getString("age_stage")),
                CitizenStatus.valueOf(resultSet.getString("status")),
                CitizenJobType.valueOf(resultSet.getString("job_type")),
                CitizenHealthState.valueOf(resultSet.getString("health_state")),
                CitizenMoraleState.valueOf(resultSet.getString("morale_state")),
                CitizenHousingState.valueOf(resultSet.getString("housing_state")),
                CitizenNutritionState.valueOf(resultSet.getString("nutrition_state")),
                CitizenTrainingState.valueOf(resultSet.getString("training_state")),
                CitizenTroopLinkState.valueOf(resultSet.getString("troop_link_state")),
                new CitizenStatBlock(
                        resultSet.getInt("strength"),
                        resultSet.getInt("endurance"),
                        resultSet.getInt("agility"),
                        resultSet.getInt("discipline"),
                        resultSet.getInt("intelligence"),
                        resultSet.getInt("morale_resilience"),
                        resultSet.getInt("work_efficiency"),
                        resultSet.getInt("combat_potential")
                ),
                resultSet.getLong("created_at_epoch_millis"),
                resultSet.getLong("updated_at_epoch_millis"),
                resultSet.getLong("last_age_stage_processed_at_epoch_millis"),
                readMetadata(resultSet.getString("metadata_json"))
        );
    }

    private String writeMetadata(Map<String, String> metadata) {
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to serialize citizen metadata.", exception);
        }
    }

    private Map<String, String> readMetadata(String metadataJson) {
        try {
            if (metadataJson == null || metadataJson.isBlank()) {
                return Map.of();
            }
            return objectMapper.readValue(
                    metadataJson,
                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class)
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to deserialize citizen metadata.", exception);
        }
    }
}
