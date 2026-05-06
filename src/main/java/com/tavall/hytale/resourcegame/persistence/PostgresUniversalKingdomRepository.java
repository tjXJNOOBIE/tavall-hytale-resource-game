package com.tavall.hytale.resourcegame.persistence;

import com.tavall.hytale.resourcegame.middleware.common.GamePlatform;
import com.tavall.hytale.resourcegame.middleware.control.ControlCommandValidationException;
import com.tavall.hytale.resourcegame.middleware.kingdom.UniversalKingdomSimulationSystem;
import com.tavall.hytale.resourcegame.middleware.kingdom.UniversalKingdomSimulationSystem.CoordinateConversionParameters;
import com.tavall.hytale.resourcegame.middleware.kingdom.UniversalKingdomSimulationSystem.EditableParameterDefinition;
import com.tavall.hytale.resourcegame.middleware.kingdom.UniversalKingdomSimulationSystem.EditableParameterValue;
import com.tavall.hytale.resourcegame.middleware.kingdom.UniversalKingdomSimulationSystem.InstanceSwitchRequest;
import com.tavall.hytale.resourcegame.middleware.kingdom.UniversalKingdomSimulationSystem.KingdomBorderDefinition;
import com.tavall.hytale.resourcegame.middleware.kingdom.UniversalKingdomSimulationSystem.KingdomId;
import com.tavall.hytale.resourcegame.middleware.kingdom.UniversalKingdomSimulationSystem.KingdomInstanceRoutingProfile;
import com.tavall.hytale.resourcegame.middleware.kingdom.UniversalKingdomSimulationSystem.KingdomStorageNamespace;
import com.tavall.hytale.resourcegame.middleware.kingdom.UniversalKingdomSimulationSystem.PlatformInstance;
import com.tavall.hytale.resourcegame.middleware.kingdom.UniversalKingdomSimulationSystem.PlayerKingdomLocation;
import com.tavall.hytale.resourcegame.middleware.kingdom.UniversalKingdomSimulationSystem.PlayerKingdomTransition;
import com.tavall.hytale.resourcegame.middleware.kingdom.UniversalKingdomSimulationSystem.UniversalKingdom;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public final class PostgresUniversalKingdomRepository implements UniversalKingdomSimulationSystem.UniversalKingdomRepository {
    private final PostgresConnectionProvider connectionProvider;
    private final ControlPlaneSnapshotJsonCodec jsonCodec;

    public PostgresUniversalKingdomRepository(PostgresConnectionProvider connectionProvider) {
        this(connectionProvider, new ControlPlaneSnapshotJsonCodec());
    }

    public PostgresUniversalKingdomRepository(PostgresConnectionProvider connectionProvider, ControlPlaneSnapshotJsonCodec jsonCodec) {
        this.connectionProvider = connectionProvider;
        this.jsonCodec = jsonCodec;
    }

    @Override
    public int nextKingdomNumber() {
        String sql = "SELECT COALESCE(MAX(sort_number), 0) + 1 FROM universal_kingdom_object_snapshot WHERE object_type = 'KINGDOM'";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException exception) {
            throw failure("read next kingdom number", exception);
        }
    }

    @Override
    public UniversalKingdom saveKingdom(UniversalKingdom kingdom) {
        save("KINGDOM", kingdom.kingdomId().value(), kingdom.kingdomId().value(), null, kingdom.kingdomNumber(), kingdom, kingdom.createdAt(), kingdom.updatedAt());
        return kingdom;
    }

    @Override
    public Optional<UniversalKingdom> findKingdom(KingdomId kingdomId) {
        return find("KINGDOM", kingdomId.value(), UniversalKingdom.class);
    }

    @Override
    public List<UniversalKingdom> kingdoms() {
        return findMany("KINGDOM", null, null, "sort_number, object_id", UniversalKingdom.class);
    }

    @Override
    public KingdomStorageNamespace saveNamespace(KingdomStorageNamespace namespace) {
        save("NAMESPACE", namespace.kingdomId().value(), namespace.kingdomId().value(), null, 0, namespace, namespace.createdAt(), Instant.now());
        return namespace;
    }

    @Override
    public Optional<KingdomStorageNamespace> findNamespace(KingdomId kingdomId) {
        return find("NAMESPACE", kingdomId.value(), KingdomStorageNamespace.class);
    }

    @Override
    public KingdomBorderDefinition saveBorder(KingdomBorderDefinition border) {
        save("BORDER", border.borderDefinitionId(), border.kingdomId().value(), null, 0, border, border.createdAt(), border.updatedAt());
        return border;
    }

    @Override
    public Optional<KingdomBorderDefinition> findBorderForKingdom(KingdomId kingdomId) {
        return findMany("BORDER", kingdomId.value(), null, "object_id", KingdomBorderDefinition.class).stream().findFirst();
    }

    @Override
    public List<KingdomBorderDefinition> kingdomBorders() {
        return findMany("BORDER", null, null, "object_id", KingdomBorderDefinition.class);
    }

    @Override
    public void saveCoordinateParameters(CoordinateConversionParameters parameters) {
        save("COORDINATE_PARAMETERS", parameters.platform().name(), null, parameters.platform(), 0, parameters, Instant.now(), Instant.now());
    }

    @Override
    public Optional<CoordinateConversionParameters> findCoordinateParameters(GamePlatform platform) {
        return find("COORDINATE_PARAMETERS", platform.name(), CoordinateConversionParameters.class);
    }

    @Override
    public PlayerKingdomLocation savePlayerLocation(PlayerKingdomLocation location) {
        save("PLAYER_LOCATION", location.universalPlayerId(), location.currentKingdomId().value(), location.platform(), 0, location, location.lastUpdatedAt(), location.lastUpdatedAt());
        return location;
    }

    @Override
    public Optional<PlayerKingdomLocation> findPlayerLocation(String universalPlayerId) {
        return find("PLAYER_LOCATION", universalPlayerId, PlayerKingdomLocation.class);
    }

    @Override
    public PlayerKingdomTransition saveTransition(PlayerKingdomTransition transition) {
        save("PLAYER_TRANSITION", transition.transitionId(), transition.universalPlayerId(), transition.platform(), 0, transition, transition.createdAt(), transition.completedAt().orElse(transition.createdAt()));
        return transition;
    }

    @Override
    public Optional<PlayerKingdomTransition> findLatestTransition(String universalPlayerId) {
        return findMany("PLAYER_TRANSITION", universalPlayerId, null, "created_at DESC, object_id DESC", PlayerKingdomTransition.class).stream().findFirst();
    }

    @Override
    public List<PlayerKingdomTransition> transitions() {
        return findMany("PLAYER_TRANSITION", null, null, "created_at, object_id", PlayerKingdomTransition.class);
    }

    @Override
    public PlatformInstance savePlatformInstance(PlatformInstance instance) {
        save("PLATFORM_INSTANCE", instance.platformInstanceId(), instance.kingdomId().value(), instance.platform(), 0, instance, instance.lastHealthCheckAt().orElse(Instant.now()), Instant.now());
        return instance;
    }

    @Override
    public Optional<PlatformInstance> findPlatformInstance(String platformInstanceId) {
        return find("PLATFORM_INSTANCE", platformInstanceId, PlatformInstance.class);
    }

    @Override
    public List<PlatformInstance> platformInstances(KingdomId kingdomId, GamePlatform platform) {
        return findMany("PLATFORM_INSTANCE", kingdomId.value(), platform, "object_id", PlatformInstance.class);
    }

    @Override
    public KingdomInstanceRoutingProfile saveRoutingProfile(KingdomInstanceRoutingProfile profile) {
        save("ROUTING_PROFILE", routingKey(profile.kingdomId(), profile.platform()), profile.kingdomId().value(), profile.platform(), 0, profile, Instant.now(), Instant.now());
        return profile;
    }

    @Override
    public Optional<KingdomInstanceRoutingProfile> findRoutingProfile(KingdomId kingdomId, GamePlatform platform) {
        return find("ROUTING_PROFILE", routingKey(kingdomId, platform), KingdomInstanceRoutingProfile.class);
    }

    @Override
    public InstanceSwitchRequest saveSwitchRequest(InstanceSwitchRequest request) {
        save("SWITCH_REQUEST", request.switchRequestId(), request.universalPlayerId(), request.platform(), 0, request, request.createdAt(), request.completedAt().orElse(request.createdAt()));
        return request;
    }

    @Override
    public Optional<InstanceSwitchRequest> findSwitchRequest(String switchRequestId) {
        return find("SWITCH_REQUEST", switchRequestId, InstanceSwitchRequest.class);
    }

    @Override
    public List<InstanceSwitchRequest> switchRequests() {
        return findMany("SWITCH_REQUEST", null, null, "created_at, object_id", InstanceSwitchRequest.class);
    }

    @Override
    public EditableParameterDefinition saveEditableParameterDefinition(EditableParameterDefinition definition) {
        save("PARAMETER_DEFINITION", definition.parameterKey(), null, null, 0, definition, Instant.now(), Instant.now());
        return definition;
    }

    @Override
    public List<EditableParameterDefinition> editableParameterDefinitions() {
        return findMany("PARAMETER_DEFINITION", null, null, "object_id", EditableParameterDefinition.class);
    }

    @Override
    public Optional<EditableParameterDefinition> findEditableParameterDefinition(String key) {
        return find("PARAMETER_DEFINITION", key, EditableParameterDefinition.class);
    }

    @Override
    public EditableParameterValue saveEditableParameterValue(EditableParameterValue value) {
        save("PARAMETER_VALUE", parameterValueKey(value.parameterKey(), value.scopeId()), value.scopeId().orElse("global"), null, 0, value, value.updatedAt(), value.updatedAt());
        return value;
    }

    @Override
    public Optional<EditableParameterValue> findEditableParameterValue(String key, Optional<String> scopeId) {
        return find("PARAMETER_VALUE", parameterValueKey(key, scopeId), EditableParameterValue.class);
    }

    private <T> Optional<T> find(String objectType, String objectId, Class<T> type) {
        String sql = "SELECT payload_json FROM universal_kingdom_object_snapshot WHERE object_type = ? AND object_id = ?";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, objectType);
            statement.setString(2, objectId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(jsonCodec.read(resultSet.getString("payload_json"), type)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw failure("read universal kingdom snapshot", exception);
        }
    }

    private <T> List<T> findMany(String objectType, String scopeId, GamePlatform platform, String orderBy, Class<T> type) {
        StringBuilder sql = new StringBuilder("SELECT payload_json FROM universal_kingdom_object_snapshot WHERE object_type = ?");
        if (scopeId != null) {
            sql.append(" AND scope_id = ?");
        }
        if (platform != null) {
            sql.append(" AND platform = ?");
        }
        sql.append(" ORDER BY ").append(orderBy);
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int index = 1;
            statement.setString(index++, objectType);
            if (scopeId != null) {
                statement.setString(index++, scopeId);
            }
            if (platform != null) {
                statement.setString(index, platform.name());
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                java.util.ArrayList<T> values = new java.util.ArrayList<>();
                while (resultSet.next()) {
                    values.add(jsonCodec.read(resultSet.getString("payload_json"), type));
                }
                return List.copyOf(values);
            }
        } catch (SQLException exception) {
            throw failure("read universal kingdom snapshots", exception);
        }
    }

    private void save(String objectType, String objectId, String scopeId, GamePlatform platform, int sortNumber, Object payload, Instant createdAt, Instant updatedAt) {
        String sql = "INSERT INTO universal_kingdom_object_snapshot "
                + "(object_type, object_id, scope_id, platform, sort_number, payload_json, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, ?::jsonb, ?, ?) "
                + "ON CONFLICT (object_type, object_id) DO UPDATE SET "
                + "scope_id = EXCLUDED.scope_id, platform = EXCLUDED.platform, sort_number = EXCLUDED.sort_number, "
                + "payload_json = EXCLUDED.payload_json, updated_at = EXCLUDED.updated_at";
        try (Connection connection = connectionProvider.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, objectType);
            statement.setString(2, objectId);
            statement.setString(3, scopeId);
            statement.setString(4, platform == null ? null : platform.name());
            statement.setInt(5, sortNumber);
            statement.setString(6, jsonCodec.write(payload));
            statement.setTimestamp(7, Timestamp.from(createdAt == null ? Instant.now() : createdAt));
            statement.setTimestamp(8, Timestamp.from(updatedAt == null ? Instant.now() : updatedAt));
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw failure("save universal kingdom snapshot", exception);
        }
    }

    private String routingKey(KingdomId kingdomId, GamePlatform platform) {
        return kingdomId.value() + ":" + platform.name();
    }

    private String parameterValueKey(String key, Optional<String> scopeId) {
        return key + ":" + scopeId.orElse("global");
    }

    private ControlCommandValidationException failure(String action, SQLException exception) {
        return new ControlCommandValidationException("Failed to " + action + ": " + exception.getMessage());
    }
}
