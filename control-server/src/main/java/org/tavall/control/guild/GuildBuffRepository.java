package org.tavall.control.guild;

import java.time.Instant;
import java.util.List;

public interface GuildBuffRepository {
    GuildUpgrade saveUpgrade(GuildUpgrade guildUpgrade);

    List<GuildUpgrade> findUpgrades(GuildId guildId);

    GuildBuff saveBuff(GuildBuff guildBuff);

    List<GuildBuff> findActiveBuffs(GuildId guildId, Instant now);
}
