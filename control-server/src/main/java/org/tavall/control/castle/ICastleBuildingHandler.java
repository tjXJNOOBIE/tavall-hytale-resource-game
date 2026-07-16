package org.tavall.control.castle;

import com.hypixel.hytale.math.vector.Vector3d;
import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;
import org.tavall.control.domain.BuildingType;
import org.tavall.control.domain.CastleBuildingData;
import org.tavall.control.domain.CastleBuildingSummary;
import org.tavall.control.domain.BuildingMutationResult;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.control.population.PromotionCost;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ICastleBuildingHandler extends IDependencyInjectableInterface {
    List<CastleBuildingData> listBuildings(PlayerGameState state);

    Optional<CastleBuildingData> resolveBuilding(PlayerGameState state, String token);

    Optional<CastleBuildingData> findBuilding(PlayerGameState state, UUID buildingId);

    CastleBuildingSummary summary(UUID playerId, PlayerGameState state, CastleBuildingData buildingData, Instant now);

    String summaryLine(UUID playerId, PlayerGameState state, CastleBuildingData buildingData, int index, Instant now);

    BuildingMutationResult placeBuilding(UUID playerId, BuildingType buildingType, String worldName, Vector3d worldPosition, Instant now);

    BuildingMutationResult spawnBuilding(UUID playerId, BuildingType buildingType, int level, String worldName, Vector3d worldPosition, Instant now);

    BuildingMutationResult startUpgrade(UUID playerId, UUID buildingId, Instant now);

    BuildingMutationResult cancelUpgrade(UUID playerId, UUID buildingId, Instant now);

    BuildingMutationResult forceComplete(UUID playerId, UUID buildingId, Instant now);

    BuildingMutationResult clearBuildings(UUID playerId, Instant now);

    PlayerGameState applyTick(UUID playerId, PlayerGameState state, Instant now);

    PromotionCost adjustedPromotionCost(PlayerGameState state, PromotionCost baseCost);

    double constructionSpeedMultiplier(PlayerGameState state);
}

