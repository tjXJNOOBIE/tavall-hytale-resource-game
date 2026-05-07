package com.tavall.hytale.resourcegame.middleware.economy;

import com.tavall.hytale.resourcegame.middleware.guild.GuildId;

import java.util.Map;

public final class GuildTreasuryBalanceHandler {
    private final EconomyRepository economyRepository;

    public GuildTreasuryBalanceHandler(EconomyRepository economyRepository) {
        this.economyRepository = economyRepository;
    }

    public GuildTreasury treasuryBalance(GuildId guildId) {
        return economyRepository.findTreasury(guildId).orElseGet(() -> new GuildTreasury(guildId, 0L, Map.of()));
    }
}
