package com.tavall.resourcegame.middleware.economy;

import com.tavall.resourcegame.middleware.guild.GuildId;

import java.util.List;

public final class GuildTaxTransactionLogHandler implements IEconomyDomain {
    public GuildTaxTransactionLogHandler() {
    }

    public GuildTaxTransactionLogHandler(EconomyRepository economyRepository) {
        registerEconomyRepository(economyRepository);
    }

    public List<GuildTaxTransaction> taxTransactions(GuildId guildId) {
        return getEconomyRepository().findTaxTransactions(guildId);
    }
}
