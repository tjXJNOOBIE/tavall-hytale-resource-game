package com.tavall.hytale.resourcegame.middleware.economy;

import com.tavall.hytale.resourcegame.middleware.guild.GuildId;

public final class GuildTaxPolicyUpdateHandler {
    private final EconomyRepository economyRepository;

    public GuildTaxPolicyUpdateHandler(EconomyRepository economyRepository) {
        this.economyRepository = economyRepository;
    }

    public TaxPolicy updateTaxPolicy(GuildId guildId, TaxPolicy taxPolicy) {
        return economyRepository.saveTaxPolicy(guildId, taxPolicy);
    }
}
