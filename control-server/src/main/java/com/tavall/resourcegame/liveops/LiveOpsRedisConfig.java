package com.tavall.resourcegame.liveops;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

public record LiveOpsRedisConfig(
        String liveConfigChannel,
        String globalGuiChannelPrefix,
        String globalGuiChannelPattern
) implements IDependencyInjectableConcrete {
    public LiveOpsRedisConfig {
        if (liveConfigChannel == null || liveConfigChannel.isBlank()) {
            liveConfigChannel = "resource-game:live-config:update";
        }
        if (globalGuiChannelPrefix == null || globalGuiChannelPrefix.isBlank()) {
            globalGuiChannelPrefix = "resource-game:gui:update:";
        }
        if (globalGuiChannelPattern == null || globalGuiChannelPattern.isBlank()) {
            globalGuiChannelPattern = "resource-game:gui:update:*";
        }
    }

    public static LiveOpsRedisConfig defaults() {
        return new LiveOpsRedisConfig(
                "resource-game:live-config:update",
                "resource-game:gui:update:",
                "resource-game:gui:update:*"
        );
    }
}
