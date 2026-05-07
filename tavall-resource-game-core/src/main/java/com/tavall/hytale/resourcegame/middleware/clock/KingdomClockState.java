package com.tavall.hytale.resourcegame.middleware.clock;

import com.tavall.hytale.resourcegame.middleware.common.MetadataMaps;

import java.time.Instant;
import java.time.LocalTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record KingdomClockState(
        String kingdomId,
        KingdomClockMode clockMode,
        long currentKingdomDay,
        int currentHour,
        int currentMinute,
        KingdomTimePhase currentPhase,
        long currentEpochMinute,
        Optional<String> timezoneId,
        KingdomClockRealTimeSource realTimeSource,
        Optional<LocalTime> timeOverride,
        Instant lastTickAt,
        Instant updatedAt,
        Map<String, String> metadata
) {
    public KingdomClockState {
        if (kingdomId == null || kingdomId.isBlank()) {
            throw new IllegalArgumentException("kingdomId is required.");
        }
        Objects.requireNonNull(clockMode, "clockMode");
        if (currentKingdomDay < 1) {
            throw new IllegalArgumentException("currentKingdomDay must be positive.");
        }
        validateClockPart("currentHour", currentHour, 23);
        validateClockPart("currentMinute", currentMinute, 59);
        Objects.requireNonNull(currentPhase, "currentPhase");
        timezoneId = timezoneId == null ? Optional.empty() : timezoneId;
        Objects.requireNonNull(realTimeSource, "realTimeSource");
        timeOverride = timeOverride == null ? Optional.empty() : timeOverride;
        Objects.requireNonNull(lastTickAt, "lastTickAt");
        Objects.requireNonNull(updatedAt, "updatedAt");
        metadata = MetadataMaps.immutable(metadata);
    }

    public KingdomClockState withClock(
            KingdomClockMode mode,
            long kingdomDay,
            int hour,
            int minute,
            KingdomTimePhase phase,
            long epochMinute,
            Optional<LocalTime> override,
            Instant now
    ) {
        return new KingdomClockState(kingdomId, mode, kingdomDay, hour, minute, phase, epochMinute, timezoneId, realTimeSource, override, now, now, metadata);
    }

    public KingdomClockState withMode(KingdomClockMode mode, Instant now) {
        return new KingdomClockState(kingdomId, mode, currentKingdomDay, currentHour, currentMinute, currentPhase, currentEpochMinute, timezoneId, realTimeSource, timeOverride, lastTickAt, now, metadata);
    }

    public boolean isDay() {
        return currentPhase == KingdomTimePhase.DAWN || currentPhase == KingdomTimePhase.DAY || currentPhase == KingdomTimePhase.DUSK;
    }

    public boolean isNight() {
        return currentPhase == KingdomTimePhase.NIGHT;
    }

    private static void validateClockPart(String fieldName, int value, int max) {
        if (value < 0 || value > max) {
            throw new IllegalArgumentException(fieldName + " is out of range.");
        }
    }
}
