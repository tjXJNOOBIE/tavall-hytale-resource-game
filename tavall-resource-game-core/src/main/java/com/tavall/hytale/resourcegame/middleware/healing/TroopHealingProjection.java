package com.tavall.hytale.resourcegame.middleware.healing;

import com.tavall.hytale.resourcegame.middleware.asset.GlobalAssetId;
import com.tavall.hytale.resourcegame.middleware.common.GamePlatform;
import com.tavall.hytale.resourcegame.middleware.common.MetadataMaps;
import com.tavall.hytale.resourcegame.middleware.projection.InteractionAction;
import com.tavall.hytale.resourcegame.middleware.troop.TroopId;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record TroopHealingProjection(
        GamePlatform platform,
        TroopId troopId,
        UUID woundId,
        WoundType woundType,
        WoundSeverity severity,
        List<TroopHealingProjectionOption> healingOptions,
        List<GlobalAssetId> globalAssetIds,
        Map<GlobalAssetId, String> platformAssetReferences,
        List<InteractionAction> interactionActions,
        Map<String, String> metadata
) {
    public TroopHealingProjection {
        Objects.requireNonNull(platform, "platform");
        Objects.requireNonNull(troopId, "troopId");
        Objects.requireNonNull(woundId, "woundId");
        Objects.requireNonNull(woundType, "woundType");
        Objects.requireNonNull(severity, "severity");
        healingOptions = healingOptions == null ? List.of() : List.copyOf(healingOptions);
        globalAssetIds = globalAssetIds == null ? List.of() : List.copyOf(globalAssetIds);
        platformAssetReferences = platformAssetReferences == null ? Map.of() : Map.copyOf(platformAssetReferences);
        interactionActions = interactionActions == null ? List.of() : List.copyOf(interactionActions);
        metadata = MetadataMaps.immutable(metadata);
    }
}
