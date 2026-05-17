package com.tavall.resourcegame.middleware.clock;

import com.tavall.resourcegame.middleware.common.MetadataMaps;

import java.util.Map;

public record MoraleScheduleModifier(
        String kingdomId,
        KingdomScheduleTargetScope targetScope,
        int modifierAmount,
        String reason,
        boolean active,
        Map<String, String> metadata
) {
    public MoraleScheduleModifier {
        reason = reason == null ? "" : reason;
        metadata = MetadataMaps.immutable(metadata);
    }
}
