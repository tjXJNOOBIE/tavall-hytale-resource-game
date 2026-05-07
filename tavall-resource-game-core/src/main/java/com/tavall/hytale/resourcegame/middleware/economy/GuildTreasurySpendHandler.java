package com.tavall.hytale.resourcegame.middleware.economy;

import com.tavall.hytale.resourcegame.middleware.common.HighRiskAction;
import com.tavall.hytale.resourcegame.middleware.guild.GuildId;
import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;
import com.tavall.hytale.resourcegame.middleware.security.HighRiskActionChallengeHandler;

import java.util.Map;

public final class GuildTreasurySpendHandler {
    private final EconomyRepository economyRepository;
    private final HighRiskActionChallengeHandler highRiskActionChallengeHandler;
    private final long largeSpendThreshold;

    public GuildTreasurySpendHandler(EconomyRepository economyRepository, HighRiskActionChallengeHandler highRiskActionChallengeHandler, long largeSpendThreshold) {
        this.economyRepository = economyRepository;
        this.highRiskActionChallengeHandler = highRiskActionChallengeHandler;
        this.largeSpendThreshold = Math.max(0L, largeSpendThreshold);
    }

    public GuildTreasury spendCoins(GuildId guildId, UniversalPlayerId actorId, long coinAmount, boolean highRiskChallengePassed) {
        if (coinAmount >= largeSpendThreshold) {
            highRiskActionChallengeHandler.validateHighRiskAction(actorId, HighRiskAction.LARGE_TREASURY_SPEND, highRiskChallengePassed);
        }
        GuildTreasury treasury = economyRepository.findTreasury(guildId).orElseGet(() -> new GuildTreasury(guildId, 0L, Map.of()));
        return economyRepository.saveTreasury(treasury.withSpentCoins(coinAmount));
    }
}
