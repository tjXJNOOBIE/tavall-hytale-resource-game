package com.tavall.hytale.resourcegame.middleware.clock;

import com.tavall.hytale.resourcegame.middleware.common.MetadataMaps;

import java.util.List;
import java.util.Map;

public record KingdomScheduleProjection(
        String kingdomId,
        List<KingdomScheduleWindow> activeWindows,
        List<String> citizenScheduleHints,
        List<String> townScheduleHints,
        List<String> interiorMoodHints,
        List<String> shopOpenCloseHints,
        List<String> troopTrainingHints,
        List<String> buildingProductivityHints,
        List<String> moraleHints,
        Map<String, String> metadata
) {
    public KingdomScheduleProjection {
        activeWindows = activeWindows == null ? List.of() : List.copyOf(activeWindows);
        citizenScheduleHints = citizenScheduleHints == null ? List.of() : List.copyOf(citizenScheduleHints);
        townScheduleHints = townScheduleHints == null ? List.of() : List.copyOf(townScheduleHints);
        interiorMoodHints = interiorMoodHints == null ? List.of() : List.copyOf(interiorMoodHints);
        shopOpenCloseHints = shopOpenCloseHints == null ? List.of() : List.copyOf(shopOpenCloseHints);
        troopTrainingHints = troopTrainingHints == null ? List.of() : List.copyOf(troopTrainingHints);
        buildingProductivityHints = buildingProductivityHints == null ? List.of() : List.copyOf(buildingProductivityHints);
        moraleHints = moraleHints == null ? List.of() : List.copyOf(moraleHints);
        metadata = MetadataMaps.immutable(metadata);
    }
}
