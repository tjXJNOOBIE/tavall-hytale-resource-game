package com.tavall.resourcegame.middleware.economy;

import com.tavall.resourcegame.middleware.guild.GuildId;

public final class GuildTaxPolicyUpdateHandler implements IEconomyDomain {
    public GuildTaxPolicyUpdateHandler() {
    }

    public GuildTaxPolicyUpdateHandler(EconomyRepository economyRepository) {
        registerEconomyRepository(economyRepository);
    }

    public TaxPolicy updateTaxPolicy(GuildId guildId, TaxPolicy taxPolicy) {
        return getEconomyRepository().saveTaxPolicy(guildId, taxPolicy);
    }
}
