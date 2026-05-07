package com.tavall.hytale.resourcegame.middleware.companion;

import java.util.Optional;
import java.util.UUID;

public record CompanionSkillSlot(
        int slotIndex,
        Optional<UUID> skillId,
        boolean unlocked,
        int requiredLevel
) {
    public CompanionSkillSlot {
        skillId = skillId == null ? Optional.empty() : skillId;
        if (slotIndex < 1) {
            throw new IllegalArgumentException("Companion skill slot index must be positive.");
        }
    }

    public CompanionSkillSlot withUnlocked(boolean value) {
        return new CompanionSkillSlot(slotIndex, skillId, value, requiredLevel);
    }

    public CompanionSkillSlot withSkill(UUID value) {
        return new CompanionSkillSlot(slotIndex, Optional.of(value), unlocked, requiredLevel);
    }
}
