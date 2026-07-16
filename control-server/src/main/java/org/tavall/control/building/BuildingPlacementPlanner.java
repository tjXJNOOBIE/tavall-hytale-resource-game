package org.tavall.control.building;
import org.tavall.control.player.PlayerGameStateHandler;

import com.hypixel.hytale.math.vector.Vector3d;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.castle.ICastleBuildingHandler;
import org.tavall.control.interior.IInteriorInstanceHandler;
import org.tavall.control.player.IPlayerGameStateHandler;
import org.tavall.control.domain.BuildingType;
import org.tavall.control.domain.CastleBuildingData;
import org.tavall.control.domain.CastleBuildingSummary;
import org.tavall.control.domain.PlayerGameState;
import org.tavall.minecraft.domain.interior.InteriorLayout;
import org.tavall.minecraft.domain.interior.InteriorLayoutHandler;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Computes deterministic world-space staging anchors for building placement and inspection.
 */
public final class BuildingPlacementPlanner implements IDependencyInjectableConcrete {
    private final ICastleBuildingHandler buildingHandler;
    private final IInteriorInstanceHandler interiorInstanceHandler;
    private final IPlayerGameStateHandler gameStateHandler;
    private final InteriorLayoutHandler interiorLayoutHandler;

    public BuildingPlacementPlanner(
            ICastleBuildingHandler buildingHandler,
            IInteriorInstanceHandler interiorInstanceHandler,
            IPlayerGameStateHandler gameStateHandler,
            InteriorLayoutHandler interiorLayoutHandler
    ) {
        this.buildingHandler = Objects.requireNonNull(buildingHandler, "buildingHandler");
        this.interiorInstanceHandler = Objects.requireNonNull(interiorInstanceHandler, "interiorInstanceHandler");
        this.gameStateHandler = Objects.requireNonNull(gameStateHandler, "gameStateHandler");
        this.interiorLayoutHandler = Objects.requireNonNull(interiorLayoutHandler, "interiorLayoutHandler");
    }

    public String recommendedWorldName(UUID playerId, PlayerGameState state, BuildingType buildingType) {
        if (playerId == null || state == null || buildingType == null) {
            return null;
        }
        Optional<CastleBuildingData> existing = buildingHandler.resolveBuilding(state, buildingType.shortKey());
        if (existing.isPresent()) {
            return buildingHandler.summary(playerId, state, existing.get(), Instant.now()).worldName();
        }
        return switch (buildingType.areaType()) {
            case CASTLE_SURFACE -> state.castleLocation() == null ? null : state.castleLocation().worldName();
            case CASTLE_INTERIOR -> interiorInstanceHandler.worldNameFor(playerId);
        };
    }

    public Vector3d recommendedPosition(UUID playerId, PlayerGameState state, BuildingType buildingType) {
        if (playerId == null || state == null || buildingType == null) {
            return null;
        }
        Optional<CastleBuildingData> existing = buildingHandler.resolveBuilding(state, buildingType.shortKey());
        if (existing.isPresent()) {
            CastleBuildingSummary summary = buildingHandler.summary(playerId, state, existing.get(), Instant.now());
            return new Vector3d(summary.worldX(), summary.worldY(), summary.worldZ());
        }
        return switch (buildingType.areaType()) {
            case CASTLE_SURFACE -> surfaceOffset(state, 8.0D, 0.0D, 8.0D);
            case CASTLE_INTERIOR -> interiorBuildingAnchor(state, buildingType);
        };
    }

    private Vector3d surfaceOffset(PlayerGameState state, double offsetX, double offsetY, double offsetZ) {
        if (state.castleLocation() == null) {
            return null;
        }
        return new Vector3d(
                state.castleLocation().x() + offsetX,
                state.castleLocation().y() + offsetY,
                state.castleLocation().z() + offsetZ
        );
    }

    private Vector3d interiorOffset(PlayerGameState state, double offsetX, double offsetY, double offsetZ) {
        int interiorIndex = gameStateHandler.interiorInstanceIndex(state);
        Vector3d origin = interiorLayoutHandler.originForCastle(state.castleLocation(), interiorIndex);
        return new Vector3d(
                origin.getX() + offsetX,
                origin.getY() + offsetY,
                origin.getZ() + offsetZ
        );
    }

    private Vector3d interiorBuildingAnchor(PlayerGameState state, BuildingType buildingType) {
        int interiorIndex = gameStateHandler.interiorInstanceIndex(state);
        InteriorLayout layout = interiorLayoutHandler.createLayoutForCastle(state.castleLocation(), interiorIndex);
        return layout.buildingAnchor(buildingType);
    }
}

