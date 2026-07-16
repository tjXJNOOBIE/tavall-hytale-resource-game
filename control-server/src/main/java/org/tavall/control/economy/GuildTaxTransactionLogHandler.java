package org.tavall.control.economy;

import org.tavall.control.guild.GuildId;

import java.util.List;

public final class GuildTaxTransactionLogHandler implements EconomyDomain {
    public GuildTaxTransactionLogHandler() {
    }

    public GuildTaxTransactionLogHandler(EconomyRepository economyRepository) {
        registerEconomyRepository(economyRepository);
    }

    public List<GuildTaxTransaction> taxTransactions(GuildId guildId) {
        return getEconomyRepository().findTaxTransactions(guildId);
    }
}
