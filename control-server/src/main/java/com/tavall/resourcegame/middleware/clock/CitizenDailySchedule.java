package com.tavall.resourcegame.middleware.clock;

import com.tavall.resourcegame.middleware.common.MetadataMaps;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public record CitizenDailySchedule(
        String citizenId,
        String kingdomId,
        KingdomScheduleWindow jobStartWindow,
        KingdomScheduleWindow jobEndWindow,
        Optional<KingdomScheduleWindow> freeTimeWindow,
        Optional<KingdomScheduleWindow> sleepWindow,
        CitizenScheduledState currentScheduledState,
        Instant updatedAt,
        Map<String, String> metadata
) {
    public CitizenDailySchedule {
        freeTimeWindow = freeTimeWindow == null ? Optional.empty() : freeTimeWindow;
        sleepWindow = sleepWindow == null ? Optional.empty() : sleepWindow;
        metadata = MetadataMaps.immutable(metadata);
    }
}
