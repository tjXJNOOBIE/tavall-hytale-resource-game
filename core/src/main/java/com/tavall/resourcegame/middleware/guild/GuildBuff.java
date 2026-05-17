package com.tavall.resourcegame.middleware.guild;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record GuildBuff(
        UUID buffId,
        GuildId guildId,
        GuildUpgradeType upgradeType,
        double modifier,
        Instant activatedAt,
        Optional<Instant> expiresAt
) {
    public GuildBuff {
        Objects.requireNonNull(buffId, "buffId");
        Objects.requireNonNull(guildId, "guildId");
        Objects.requireNonNull(upgradeType, "upgradeType");
        expiresAt = expiresAt == null ? Optional.empty() : expiresAt;
        Objects.requireNonNull(activatedAt, "activatedAt");
    }

    public boolean activeAt(Instant now) {
        return expiresAt.map(expiry -> expiry.isAfter(now)).orElse(true);
    }
}
