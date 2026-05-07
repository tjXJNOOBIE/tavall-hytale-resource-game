package com.tavall.hytale.resourcegame.middleware.guild;

import java.time.Instant;

public final class GuildUpgradePurchaseHandler {
    private final GuildBuffRepository guildBuffRepository;

    public GuildUpgradePurchaseHandler(GuildBuffRepository guildBuffRepository) {
        this.guildBuffRepository = guildBuffRepository;
    }

    public GuildUpgrade purchaseGuildUpgrade(GuildId guildId, GuildUpgradeType upgradeType, int level, Instant now) {
        return guildBuffRepository.saveUpgrade(new GuildUpgrade(guildId, upgradeType, level, now));
    }
}
