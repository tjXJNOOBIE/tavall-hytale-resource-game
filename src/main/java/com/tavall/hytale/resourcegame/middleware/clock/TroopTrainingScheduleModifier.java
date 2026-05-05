package com.tavall.hytale.resourcegame.middleware.clock;

import com.tavall.hytale.resourcegame.middleware.common.MetadataMaps;

import java.util.Map;

public record TroopTrainingScheduleModifier(
        String kingdomId,
        boolean active,
        KingdomTimePhase currentPhase,
        double speedModifier,
        String reason,
        Map<String, String> metadata
) {
    public TroopTrainingScheduleModifier {
        reason = reason == null ? "" : reason;
        metadata = MetadataMaps.immutable(metadata);
    }
}
