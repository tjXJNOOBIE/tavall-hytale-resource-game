package org.tavall.control.economy;

import org.tavall.control.guild.GuildId;

public final class GuildTaxPolicyUpdateHandler implements EconomyDomain {
    public GuildTaxPolicyUpdateHandler() {
    }

    public GuildTaxPolicyUpdateHandler(EconomyRepository economyRepository) {
        registerEconomyRepository(economyRepository);
    }

    public TaxPolicy updateTaxPolicy(GuildId guildId, TaxPolicy taxPolicy) {
        return getEconomyRepository().saveTaxPolicy(guildId, taxPolicy);
    }
}
