package org.tavall.control.support;

import com.hypixel.hytale.math.vector.Vector3d;
import org.tavall.control.dependency.interfaces.ICastleBuildingService;
import org.tavall.control.domain.BuildingConstructionStage;
import org.tavall.control.domain.BuildingLevelProfile;
import org.tavall.control.domain.BuildingMutationResult;
import org.tavall.control.domain.BuildingType;
import org.tavall.control.domain.CastleBuildingData;
import org.tavall.control.domain.CastleBuildingSummary;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.population.PromotionCost;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class NoopCastleBuildingService implements ICastleBuildingService {
    @Override
    public List<CastleBuildingData> listBuildings(PlayerGameState state) {
        return List.of();
    }

    @Override
    public Optional<CastleBuildingData> resolveBuilding(PlayerGameState state, String token) {
        return Optional.empty();
    }

    @Override
    public Optional<CastleBuildingData> findBuilding(PlayerGameState state, UUID buildingId) {
        return Optional.empty();
    }

    @Override
    public CastleBuildingSummary summary(UUID playerId, PlayerGameState state, CastleBuildingData buildingData, Instant now) {
        BuildingLevelProfile profile = buildingData.currentLevel() <= 0 ? null : buildingData.buildingType().levelProfile(buildingData.currentLevel());
        return new CastleBuildingSummary(
                buildingData,
                state.castleLocation() == null ? "default" : state.castleLocation().worldName(),
                state.castleLocation() == null ? 0.0D : state.castleLocation().x(),
                state.castleLocation() == null ? 0.0D : state.castleLocation().y(),
                state.castleLocation() == null ? 0.0D : state.castleLocation().z(),
                buildingData.isUnderConstruction() ? 0.0D : 1.0D,
                0L,
                buildingData.isUnderConstruction() ? BuildingConstructionStage.FOUNDATION : BuildingConstructionStage.COMPLETE,
                profile,
                profile,
                null
        );
    }

    @Override
    public String summaryLine(UUID playerId, PlayerGameState state, CastleBuildingData buildingData, int index, Instant now) {
        return "#" + index + " " + buildingData.buildingType().displayName();
    }

    @Override
    public BuildingMutationResult placeBuilding(UUID playerId, BuildingType buildingType, String worldName, Vector3d worldPosition, Instant now) {
        return BuildingMutationResult.unchanged(null, "noop");
    }

    @Override
    public BuildingMutationResult spawnBuilding(UUID playerId, BuildingType buildingType, int level, String worldName, Vector3d worldPosition, Instant now) {
        return BuildingMutationResult.unchanged(null, "noop");
    }

    @Override
    public BuildingMutationResult startUpgrade(UUID playerId, UUID buildingId, Instant now) {
        return BuildingMutationResult.unchanged(null, "noop");
    }

    @Override
    public BuildingMutationResult cancelUpgrade(UUID playerId, UUID buildingId, Instant now) {
        return BuildingMutationResult.unchanged(null, "noop");
    }

    @Override
    public BuildingMutationResult forceComplete(UUID playerId, UUID buildingId, Instant now) {
        return BuildingMutationResult.unchanged(null, "noop");
    }

    @Override
    public BuildingMutationResult clearBuildings(UUID playerId, Instant now) {
        return BuildingMutationResult.unchanged(null, "noop");
    }

    @Override
    public PlayerGameState applyTick(UUID playerId, PlayerGameState state, Instant now) {
        return state;
    }

    @Override
    public PromotionCost adjustedPromotionCost(PlayerGameState state, PromotionCost baseCost) {
        return baseCost;
    }

    @Override
    public double constructionSpeedMultiplier(PlayerGameState state) {
        return 1.0D;
    }
}
