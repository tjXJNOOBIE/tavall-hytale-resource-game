package com.tavall.resourcegame.middleware.clock;

import com.tavall.resourcegame.middleware.common.MetadataMaps;

import java.util.Map;
import java.util.Optional;

public record BuildingProductivityScheduleModifier(
        String kingdomId,
        Optional<String> buildingId,
        boolean active,
        double productivityModifier,
        String reason,
        Map<String, String> metadata
) {
    public BuildingProductivityScheduleModifier {
        buildingId = buildingId == null ? Optional.empty() : buildingId;
        reason = reason == null ? "" : reason;
        metadata = MetadataMaps.immutable(metadata);
    }
}
