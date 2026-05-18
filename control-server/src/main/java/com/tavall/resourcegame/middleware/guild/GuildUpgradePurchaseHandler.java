package org.tavall.control.guild;

import java.time.Instant;

public final class GuildUpgradePurchaseHandler implements IGuildDomain {
    public GuildUpgradePurchaseHandler() {
    }

    public GuildUpgradePurchaseHandler(GuildBuffRepository guildBuffRepository) {
        registerGuildBuffRepository(guildBuffRepository);
    }

    public GuildUpgrade purchaseGuildUpgrade(GuildId guildId, GuildUpgradeType upgradeType, int level, Instant now) {
        return getGuildBuffRepository().saveUpgrade(new GuildUpgrade(guildId, upgradeType, level, now));
    }
}
