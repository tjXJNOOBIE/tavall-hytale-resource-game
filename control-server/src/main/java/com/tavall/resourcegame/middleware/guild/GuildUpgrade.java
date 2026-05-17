package com.tavall.resourcegame.middleware.guild;

import java.time.Instant;
import java.util.Objects;

public record GuildUpgrade(
        GuildId guildId,
        GuildUpgradeType upgradeType,
        int level,
        Instant purchasedAt
) {
    public GuildUpgrade {
        Objects.requireNonNull(guildId, "guildId");
        Objects.requireNonNull(upgradeType, "upgradeType");
        level = Math.max(1, level);
        Objects.requireNonNull(purchasedAt, "purchasedAt");
    }
}
