package com.tavall.hytale.resourcegame.middleware.guild;

import java.time.Instant;
import java.util.List;

public record GuildStatCalculationContext(
        double baseStat,
        GuildJobDomain actionDomain,
        GuildMemberProfile actor,
        KingdomState kingdomState,
        List<GuildUpgrade> upgrades,
        List<GuildBuff> activeBuffs,
        Instant now
) {
    public GuildStatCalculationContext {
        kingdomState = kingdomState == null ? KingdomState.PROTECTED : kingdomState;
        upgrades = upgrades == null ? List.of() : List.copyOf(upgrades);
        activeBuffs = activeBuffs == null ? List.of() : List.copyOf(activeBuffs);
    }
}
