package com.tavall.resourcegame.middleware.economy;

import com.tavall.resourcegame.middleware.guild.GuildId;

import java.util.List;
import java.util.Optional;

public interface EconomyRepository {
    TaxPolicy saveTaxPolicy(GuildId guildId, TaxPolicy taxPolicy);

    Optional<TaxPolicy> findTaxPolicy(GuildId guildId);

    GuildTreasury saveTreasury(GuildTreasury guildTreasury);

    Optional<GuildTreasury> findTreasury(GuildId guildId);

    GuildTaxTransaction saveTaxTransaction(GuildTaxTransaction transaction);

    List<GuildTaxTransaction> findTaxTransactions(GuildId guildId);
}
