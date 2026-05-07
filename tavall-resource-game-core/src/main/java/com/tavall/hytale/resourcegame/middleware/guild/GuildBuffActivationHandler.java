package com.tavall.hytale.resourcegame.middleware.guild;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public final class GuildBuffActivationHandler {
    private final GuildBuffRepository guildBuffRepository;

    public GuildBuffActivationHandler(GuildBuffRepository guildBuffRepository) {
        this.guildBuffRepository = guildBuffRepository;
    }

    public GuildBuff activateGuildBuff(GuildId guildId, GuildUpgradeType upgradeType, double modifier, Duration duration, Instant now) {
        GuildBuff buff = new GuildBuff(
                UUID.randomUUID(),
                guildId,
                upgradeType,
                modifier,
                now,
                duration == null ? Optional.empty() : Optional.of(now.plus(duration))
        );
        return guildBuffRepository.saveBuff(buff);
    }
}
