package com.tavall.resourcegame.middleware.clock;

import com.tavall.resourcegame.middleware.common.MetadataMaps;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public record KingdomScheduleWindow(
        String windowId,
        String displayName,
        int startHour,
        int startMinute,
        int endHour,
        int endMinute,
        Set<KingdomTimePhase> phases,
        boolean wrapsMidnight,
        Map<String, String> metadata
) {
    public KingdomScheduleWindow {
        if (windowId == null || windowId.isBlank()) {
            throw new IllegalArgumentException("windowId is required.");
        }
        displayName = displayName == null || displayName.isBlank() ? windowId : displayName;
        validateClockPart("startHour", startHour, 23);
        validateClockPart("endHour", endHour, 23);
        validateClockPart("startMinute", startMinute, 59);
        validateClockPart("endMinute", endMinute, 59);
        phases = phases == null ? Set.of() : Set.copyOf(phases);
        metadata = MetadataMaps.immutable(metadata);
    }

    public static KingdomScheduleWindow of(String windowId, String displayName, int startHour, int endHour) {
        boolean wraps = startHour > endHour;
        return new KingdomScheduleWindow(windowId, displayName, startHour, 0, endHour, 0, Set.of(), wraps, Map.of());
    }

    public boolean contains(int hour, int minute, KingdomTimePhase phase) {
        Objects.requireNonNull(phase, "phase");
        if (!phases.isEmpty() && !phases.contains(phase)) {
            return false;
        }
        int current = hour * 60 + minute;
        int start = startHour * 60 + startMinute;
        int end = endHour * 60 + endMinute;
        if (start == end) {
            return true;
        }
        boolean wraps = wrapsMidnight || start > end;
        if (wraps) {
            return current >= start || current < end;
        }
        return current >= start && current < end;
    }

    private static void validateClockPart(String fieldName, int value, int max) {
        if (value < 0 || value > max) {
            throw new IllegalArgumentException(fieldName + " is out of range.");
        }
    }
}
