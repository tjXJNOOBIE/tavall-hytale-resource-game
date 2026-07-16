package org.tavall.control.clock;

import org.tavall.control.common.MetadataMaps;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record KingdomClockConfig(
        Optional<String> kingdomId,
        KingdomClockMode clockMode,
        String timezoneId,
        boolean useServerLocalTimezone,
        int realMinutesPerKingdomDay,
        double acceleratedTimeMultiplier,
        int dawnStartHour,
        int dayStartHour,
        int duskStartHour,
        int nightStartHour,
        int agingTickIntervalMinutes,
        int scheduleEvaluationIntervalMinutes,
        int visualBroadcastIntervalMinutes,
        boolean allowDebugOverrides,
        Map<String, String> metadata
) {
    public KingdomClockConfig {
        kingdomId = kingdomId == null ? Optional.empty() : kingdomId;
        Objects.requireNonNull(clockMode, "clockMode");
        timezoneId = timezoneId == null || timezoneId.isBlank() ? "UTC" : timezoneId;
        if (realMinutesPerKingdomDay <= 0) {
            throw new IllegalArgumentException("realMinutesPerKingdomDay must be positive.");
        }
        if (acceleratedTimeMultiplier <= 0.0d) {
            throw new IllegalArgumentException("acceleratedTimeMultiplier must be positive.");
        }
        validateHour("dawnStartHour", dawnStartHour);
        validateHour("dayStartHour", dayStartHour);
        validateHour("duskStartHour", duskStartHour);
        validateHour("nightStartHour", nightStartHour);
        if (agingTickIntervalMinutes <= 0 || scheduleEvaluationIntervalMinutes <= 0 || visualBroadcastIntervalMinutes <= 0) {
            throw new IllegalArgumentException("Clock intervals must be positive.");
        }
        metadata = MetadataMaps.immutable(metadata);
    }

    public static KingdomClockConfig defaults() {
        return new KingdomClockConfig(
                Optional.empty(),
                KingdomClockMode.REAL_TIME_SYNCED,
                "UTC",
                false,
                24 * 60,
                12.0d,
                5,
                7,
                18,
                21,
                60,
                5,
                5,
                true,
                Map.of("persistence", "in-memory")
        );
    }

    public KingdomClockConfig forKingdom(String kingdomId) {
        return new KingdomClockConfig(Optional.of(kingdomId), clockMode, timezoneId, useServerLocalTimezone, realMinutesPerKingdomDay,
                acceleratedTimeMultiplier, dawnStartHour, dayStartHour, duskStartHour, nightStartHour, agingTickIntervalMinutes,
                scheduleEvaluationIntervalMinutes, visualBroadcastIntervalMinutes, allowDebugOverrides, metadata);
    }

    public KingdomClockConfig withMode(KingdomClockMode mode) {
        return new KingdomClockConfig(kingdomId, mode, timezoneId, useServerLocalTimezone, realMinutesPerKingdomDay,
                acceleratedTimeMultiplier, dawnStartHour, dayStartHour, duskStartHour, nightStartHour, agingTickIntervalMinutes,
                scheduleEvaluationIntervalMinutes, visualBroadcastIntervalMinutes, allowDebugOverrides, metadata);
    }

    public KingdomClockConfig withTimezone(String newTimezoneId) {
        return new KingdomClockConfig(kingdomId, clockMode, newTimezoneId, useServerLocalTimezone, realMinutesPerKingdomDay,
                acceleratedTimeMultiplier, dawnStartHour, dayStartHour, duskStartHour, nightStartHour, agingTickIntervalMinutes,
                scheduleEvaluationIntervalMinutes, visualBroadcastIntervalMinutes, allowDebugOverrides, metadata);
    }

    public KingdomClockConfig withRealMinutesPerKingdomDay(int value) {
        return new KingdomClockConfig(kingdomId, clockMode, timezoneId, useServerLocalTimezone, value,
                acceleratedTimeMultiplier, dawnStartHour, dayStartHour, duskStartHour, nightStartHour, agingTickIntervalMinutes,
                scheduleEvaluationIntervalMinutes, visualBroadcastIntervalMinutes, allowDebugOverrides, metadata);
    }

    public KingdomClockConfig withAcceleratedTimeMultiplier(double value) {
        return new KingdomClockConfig(kingdomId, clockMode, timezoneId, useServerLocalTimezone, realMinutesPerKingdomDay,
                value, dawnStartHour, dayStartHour, duskStartHour, nightStartHour, agingTickIntervalMinutes,
                scheduleEvaluationIntervalMinutes, visualBroadcastIntervalMinutes, allowDebugOverrides, metadata);
    }

    public KingdomClockConfig withPhaseStarts(int dawn, int day, int dusk, int night) {
        return new KingdomClockConfig(kingdomId, clockMode, timezoneId, useServerLocalTimezone, realMinutesPerKingdomDay,
                acceleratedTimeMultiplier, dawn, day, dusk, night, agingTickIntervalMinutes,
                scheduleEvaluationIntervalMinutes, visualBroadcastIntervalMinutes, allowDebugOverrides, metadata);
    }

    public KingdomClockConfig withAgingTickIntervalMinutes(int value) {
        return new KingdomClockConfig(kingdomId, clockMode, timezoneId, useServerLocalTimezone, realMinutesPerKingdomDay,
                acceleratedTimeMultiplier, dawnStartHour, dayStartHour, duskStartHour, nightStartHour, value,
                scheduleEvaluationIntervalMinutes, visualBroadcastIntervalMinutes, allowDebugOverrides, metadata);
    }

    private static void validateHour(String fieldName, int value) {
        if (value < 0 || value > 23) {
            throw new IllegalArgumentException(fieldName + " must be between 0 and 23.");
        }
    }
}
