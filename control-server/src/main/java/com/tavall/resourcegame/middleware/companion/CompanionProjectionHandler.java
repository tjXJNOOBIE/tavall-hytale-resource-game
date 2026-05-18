package org.tavall.control.companion;

import java.util.List;
import java.util.Map;

public final class CompanionProjectionHandler {
    public CompanionOverviewProjection projectCompanion(CompanionData companion) {
        return new CompanionOverviewProjection(
                companion.companionId(),
                companion.ownerPlayerId(),
                companion.type().name() + " Companion",
                companion.type(),
                companion.status(),
                companion.behaviorState(),
                companion.moraleState(),
                companion.level(),
                companion.xp(),
                companion.skillSlots(),
                "companion." + companion.type().name().toLowerCase(),
                actions(companion),
                Map.of(
                        "hp", Double.toString(companion.calculatedStats().hp()),
                        "earthAttack", Double.toString(companion.calculatedStats().earthAttack()),
                        "arcaneAttack", Double.toString(companion.calculatedStats().arcaneAttack())
                )
        );
    }

    private List<String> actions(CompanionData companion) {
        if (companion.status() == CompanionStatus.TRAINING) {
            return List.of("companion.training.claim", "companion.training.cancel");
        }
        return List.of("companion.follow", "companion.idle", "companion.train", "companion.wall.assign", "companion.skills.open", "companion.wisdom.open", "companion.recall");
    }
}
