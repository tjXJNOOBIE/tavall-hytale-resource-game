package com.tavall.hytale.resourcegame.middleware.economy;

import com.tavall.hytale.resourcegame.middleware.guild.GuildId;

import java.util.List;

public final class GuildTaxTransactionLogHandler {
    private final EconomyRepository economyRepository;

    public GuildTaxTransactionLogHandler(EconomyRepository economyRepository) {
        this.economyRepository = economyRepository;
    }

    public List<GuildTaxTransaction> taxTransactions(GuildId guildId) {
        return economyRepository.findTaxTransactions(guildId);
    }
}
