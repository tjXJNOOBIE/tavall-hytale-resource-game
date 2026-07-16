package org.tavall.control.guild;

import org.tavall.control.node.MiddlewareResourceType;

import java.util.EnumMap;
import java.util.Map;

public record GuildTreasurySnapshot(
        long coinBalance,
        Map<MiddlewareResourceType, Integer> resourceBalances
) {
    public GuildTreasurySnapshot {
        EnumMap<MiddlewareResourceType, Integer> copy = new EnumMap<>(MiddlewareResourceType.class);
        if (resourceBalances != null) {
            copy.putAll(resourceBalances);
        }
        resourceBalances = Map.copyOf(copy);
    }

    public static GuildTreasurySnapshot empty() {
        return new GuildTreasurySnapshot(0L, Map.of());
    }
}
