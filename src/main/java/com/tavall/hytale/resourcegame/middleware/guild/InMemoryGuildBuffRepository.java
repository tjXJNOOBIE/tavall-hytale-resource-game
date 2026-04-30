package com.tavall.hytale.resourcegame.middleware.guild;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryGuildBuffRepository implements GuildBuffRepository {
    private final Map<GuildId, List<GuildUpgrade>> upgradesByGuild = new ConcurrentHashMap<>();
    private final Map<GuildId, List<GuildBuff>> buffsByGuild = new ConcurrentHashMap<>();

    @Override
    public GuildUpgrade saveUpgrade(GuildUpgrade guildUpgrade) {
        upgradesByGuild.computeIfAbsent(guildUpgrade.guildId(), ignored -> new ArrayList<>()).add(guildUpgrade);
        return guildUpgrade;
    }

    @Override
    public List<GuildUpgrade> findUpgrades(GuildId guildId) {
        return List.copyOf(upgradesByGuild.getOrDefault(guildId, List.of()));
    }

    @Override
    public GuildBuff saveBuff(GuildBuff guildBuff) {
        buffsByGuild.computeIfAbsent(guildBuff.guildId(), ignored -> new ArrayList<>()).add(guildBuff);
        return guildBuff;
    }

    @Override
    public List<GuildBuff> findActiveBuffs(GuildId guildId, Instant now) {
        return buffsByGuild.getOrDefault(guildId, List.of()).stream()
                .filter(buff -> buff.activeAt(now))
                .toList();
    }
}
