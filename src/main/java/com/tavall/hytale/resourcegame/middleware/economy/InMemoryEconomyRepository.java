package com.tavall.hytale.resourcegame.middleware.economy;

import com.tavall.hytale.resourcegame.middleware.guild.GuildId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryEconomyRepository implements EconomyRepository {
    private final Map<GuildId, TaxPolicy> policiesByGuild = new ConcurrentHashMap<>();
    private final Map<GuildId, GuildTreasury> treasuriesByGuild = new ConcurrentHashMap<>();
    private final Map<GuildId, List<GuildTaxTransaction>> transactionsByGuild = new ConcurrentHashMap<>();

    @Override
    public TaxPolicy saveTaxPolicy(GuildId guildId, TaxPolicy taxPolicy) {
        policiesByGuild.put(guildId, taxPolicy);
        return taxPolicy;
    }

    @Override
    public Optional<TaxPolicy> findTaxPolicy(GuildId guildId) {
        return Optional.ofNullable(policiesByGuild.get(guildId));
    }

    @Override
    public GuildTreasury saveTreasury(GuildTreasury guildTreasury) {
        treasuriesByGuild.put(guildTreasury.guildId(), guildTreasury);
        return guildTreasury;
    }

    @Override
    public Optional<GuildTreasury> findTreasury(GuildId guildId) {
        return Optional.ofNullable(treasuriesByGuild.get(guildId));
    }

    @Override
    public GuildTaxTransaction saveTaxTransaction(GuildTaxTransaction transaction) {
        transactionsByGuild.computeIfAbsent(transaction.guildId(), ignored -> new ArrayList<>()).add(transaction);
        return transaction;
    }

    @Override
    public List<GuildTaxTransaction> findTaxTransactions(GuildId guildId) {
        return List.copyOf(transactionsByGuild.getOrDefault(guildId, List.of()));
    }
}
