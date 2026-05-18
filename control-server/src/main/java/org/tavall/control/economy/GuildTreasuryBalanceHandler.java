package org.tavall.control.economy;

import org.tavall.control.guild.GuildId;

import java.util.Map;

public final class GuildTreasuryBalanceHandler implements IEconomyDomain {
    public GuildTreasuryBalanceHandler() {
    }

    public GuildTreasuryBalanceHandler(EconomyRepository economyRepository) {
        registerEconomyRepository(economyRepository);
    }

    public GuildTreasury treasuryBalance(GuildId guildId) {
        return getEconomyRepository().findTreasury(guildId).orElseGet(() -> new GuildTreasury(guildId, 0L, Map.of()));
    }
}
