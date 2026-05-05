package com.tavall.hytale.resourcegame.middleware.clock;

import com.tavall.hytale.resourcegame.middleware.common.MetadataMaps;

import java.util.Map;
import java.util.Optional;

public record AgingTickPolicy(
        Optional<String> kingdomId,
        boolean enabled,
        int realMinutesPerAgeIncrement,
        int ageIncrementAmount,
        boolean appliesToCitizens,
        boolean appliesToCompanions,
        Map<String, String> metadata
) {
    public AgingTickPolicy {
        kingdomId = kingdomId == null ? Optional.empty() : kingdomId;
        if (realMinutesPerAgeIncrement <= 0) {
            throw new IllegalArgumentException("realMinutesPerAgeIncrement must be positive.");
        }
        if (ageIncrementAmount <= 0) {
            throw new IllegalArgumentException("ageIncrementAmount must be positive.");
        }
        metadata = MetadataMaps.immutable(metadata);
    }

    public static AgingTickPolicy defaults() {
        return new AgingTickPolicy(Optional.empty(), true, 60, 1, true, false, Map.of("persistence", "in-memory"));
    }

    public AgingTickPolicy forKingdom(String kingdomId) {
        return new AgingTickPolicy(Optional.of(kingdomId), enabled, realMinutesPerAgeIncrement, ageIncrementAmount, appliesToCitizens, appliesToCompanions, metadata);
    }

    public AgingTickPolicy withEnabled(boolean value) {
        return new AgingTickPolicy(kingdomId, value, realMinutesPerAgeIncrement, ageIncrementAmount, appliesToCitizens, appliesToCompanions, metadata);
    }

    public AgingTickPolicy withRealMinutesPerAgeIncrement(int value) {
        return new AgingTickPolicy(kingdomId, enabled, value, ageIncrementAmount, appliesToCitizens, appliesToCompanions, metadata);
    }
}
