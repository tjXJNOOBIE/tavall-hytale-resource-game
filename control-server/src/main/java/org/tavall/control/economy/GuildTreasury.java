package org.tavall.control.economy;

import org.tavall.control.guild.GuildId;
import org.tavall.control.node.MiddlewareResourceType;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public record GuildTreasury(
        GuildId guildId,
        long coinBalance,
        Map<MiddlewareResourceType, Integer> resourceBalances
) {
    public GuildTreasury {
        Objects.requireNonNull(guildId, "guildId");
        coinBalance = Math.max(0L, coinBalance);
        EnumMap<MiddlewareResourceType, Integer> copy = new EnumMap<>(MiddlewareResourceType.class);
        if (resourceBalances != null) {
            for (Map.Entry<MiddlewareResourceType, Integer> entry : resourceBalances.entrySet()) {
                copy.put(entry.getKey(), Math.max(0, entry.getValue() == null ? 0 : entry.getValue()));
            }
        }
        resourceBalances = Map.copyOf(copy);
    }

    public GuildTreasury withAddedResource(MiddlewareResourceType resourceType, int amount) {
        EnumMap<MiddlewareResourceType, Integer> updated = new EnumMap<>(MiddlewareResourceType.class);
        updated.putAll(resourceBalances);
        updated.put(resourceType, updated.getOrDefault(resourceType, 0) + Math.max(0, amount));
        return new GuildTreasury(guildId, coinBalance, updated);
    }

    public GuildTreasury withAddedCoins(long amount) {
        return new GuildTreasury(guildId, coinBalance + Math.max(0L, amount), resourceBalances);
    }

    public GuildTreasury withSpentCoins(long amount) {
        if (amount > coinBalance) {
            throw new EconomyValidationException("Guild treasury has insufficient coins.");
        }
        return new GuildTreasury(guildId, coinBalance - Math.max(0L, amount), resourceBalances);
    }
}
