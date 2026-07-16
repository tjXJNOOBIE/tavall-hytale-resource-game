package org.tavall.control.companion;

import java.util.ArrayList;
import java.util.List;

public final class CompanionLevelingHandler {
    public static final int MAX_LEVEL = 70;

    public int levelForXp(long xp) {
        int level = 1;
        while (level < MAX_LEVEL && xp >= xpRequiredForLevel(level + 1)) {
            level++;
        }
        return level;
    }

    public long xpRequiredForLevel(int level) {
        if (level <= 1) {
            return 0;
        }
        return (long) (level - 1) * (level - 1) * 100L;
    }

    public List<CompanionSkillSlot> unlockSlotsForLevel(List<CompanionSkillSlot> slots, int level) {
        ArrayList<CompanionSkillSlot> updatedSlots = new ArrayList<>();
        for (CompanionSkillSlot slot : slots) {
            updatedSlots.add(slot.requiredLevel() <= level ? slot.withUnlocked(true) : slot);
        }
        return List.copyOf(updatedSlots);
    }
}
