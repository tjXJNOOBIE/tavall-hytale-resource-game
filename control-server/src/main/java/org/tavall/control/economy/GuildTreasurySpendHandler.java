package org.tavall.control.economy;

import org.tavall.control.common.HighRiskAction;
import org.tavall.control.guild.GuildId;
import org.tavall.control.identity.UniversalPlayerId;
import org.tavall.control.security.HighRiskActionChallengeHandler;

import java.util.Map;

public final class GuildTreasurySpendHandler implements EconomyDomain {
    private static final long LARGE_SPEND_THRESHOLD = 50_000L;
    private long largeSpendThreshold = LARGE_SPEND_THRESHOLD;

    public GuildTreasurySpendHandler() {
    }

    public GuildTreasurySpendHandler(EconomyRepository economyRepository, HighRiskActionChallengeHandler highRiskActionChallengeHandler, long largeSpendThreshold) {
        registerEconomyRepository(economyRepository);
        registerHighRiskActionChallengeHandler(highRiskActionChallengeHandler);
        this.largeSpendThreshold = Math.max(0L, largeSpendThreshold);
    }

    public GuildTreasury spendCoins(GuildId guildId, UniversalPlayerId actorId, long coinAmount, boolean highRiskChallengePassed) {
        if (coinAmount >= largeSpendThreshold) {
            getHighRiskActionChallengeHandler().validateHighRiskAction(actorId, HighRiskAction.LARGE_TREASURY_SPEND, highRiskChallengePassed);
        }
        GuildTreasury treasury = getEconomyRepository().findTreasury(guildId).orElseGet(() -> new GuildTreasury(guildId, 0L, Map.of()));
        return getEconomyRepository().saveTreasury(treasury.withSpentCoins(coinAmount));
    }
}
