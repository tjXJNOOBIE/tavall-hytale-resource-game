package com.tavall.hytale.resourcegame.middleware.companion;

import java.util.Set;
import java.util.UUID;

public record CompanionSkill(
        UUID skillId,
        String name,
        SkillAffinity affinity,
        Set<CompanionType> requiredCompanionTypes,
        int requiredLevel,
        int cooldownSeconds,
        int resourceCost,
        int skillLevel,
        int maxSkillLevel,
        CompanionSkillTargetType targetType,
        CompanionSkillEffectType effectType
) {
    public CompanionSkill {
        requiredCompanionTypes = Set.copyOf(requiredCompanionTypes);
        if (requiredLevel < 1 || cooldownSeconds < 0 || resourceCost < 0 || skillLevel < 1 || maxSkillLevel < skillLevel) {
            throw new IllegalArgumentException("Invalid companion skill configuration.");
        }
    }

    public CompanionSkill withSkillLevel(int value) {
        return new CompanionSkill(skillId, name, affinity, requiredCompanionTypes, requiredLevel, cooldownSeconds, resourceCost, value, maxSkillLevel, targetType, effectType);
    }
}
