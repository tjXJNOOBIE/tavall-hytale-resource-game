package com.tavall.resourcegame.middleware.clock;

import com.tavall.resourcegame.middleware.common.MetadataMaps;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record KingdomScheduleRule(
        String scheduleRuleId,
        Optional<String> kingdomId,
        KingdomScheduleRuleType ruleType,
        String displayName,
        boolean enabled,
        KingdomScheduleWindow window,
        int priority,
        KingdomScheduleTargetScope targetScope,
        Optional<String> targetObjectType,
        Optional<String> targetObjectId,
        KingdomScheduledEffectType effectType,
        Map<String, String> effectPayload,
        Instant createdAt,
        Instant updatedAt,
        Map<String, String> metadata
) {
    public KingdomScheduleRule {
        if (scheduleRuleId == null || scheduleRuleId.isBlank()) {
            throw new IllegalArgumentException("scheduleRuleId is required.");
        }
        kingdomId = kingdomId == null ? Optional.empty() : kingdomId;
        Objects.requireNonNull(ruleType, "ruleType");
        displayName = displayName == null || displayName.isBlank() ? ruleType.name() : displayName;
        Objects.requireNonNull(window, "window");
        Objects.requireNonNull(targetScope, "targetScope");
        targetObjectType = targetObjectType == null ? Optional.empty() : targetObjectType;
        targetObjectId = targetObjectId == null ? Optional.empty() : targetObjectId;
        Objects.requireNonNull(effectType, "effectType");
        effectPayload = MetadataMaps.immutable(effectPayload);
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(updatedAt, "updatedAt");
        metadata = MetadataMaps.immutable(metadata);
    }

    public KingdomScheduleRule withEnabled(boolean newEnabled, Instant now) {
        return new KingdomScheduleRule(scheduleRuleId, kingdomId, ruleType, displayName, newEnabled, window, priority, targetScope,
                targetObjectType, targetObjectId, effectType, effectPayload, createdAt, now, metadata);
    }
}
