package org.tavall.control.liveops.config;

import java.util.Map;
import java.util.Set;

public record LiveConfigRolloutStrategy(
        RolloutStrategyType strategyType,
        Set<String> targetIds,
        int percentage,
        Map<String, String> metadata
) {
    public LiveConfigRolloutStrategy {
        strategyType = strategyType == null ? RolloutStrategyType.GLOBAL : strategyType;
        targetIds = targetIds == null ? Set.of() : Set.copyOf(targetIds);
        percentage = Math.max(0, Math.min(100, percentage));
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static LiveConfigRolloutStrategy global() {
        return new LiveConfigRolloutStrategy(RolloutStrategyType.GLOBAL, Set.of(), 100, Map.of());
    }
}
