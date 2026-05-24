package org.tavall.api.minecraft.guild;

import org.tavall.dependency.IDependencyInjectableInterface;

import java.time.Instant;
import java.util.Map;

public interface IGuildBankDataBuilder extends IDependencyInjectableInterface {
    IGuildBankDataBuilder guildId(GuildId guildId);

    IGuildBankDataBuilder coinBalance(long coinBalance);

    IGuildBankDataBuilder resourceBalances(Map<GuildResourceType, Integer> resourceBalances);

    IGuildBankDataBuilder updatedAt(Instant updatedAt);

    IGuildBankDataBuilder metadata(Map<String, String> metadata);

    IGuildBankDataBuilder copyOf(GuildBankData guildBankData);

    GuildBankData build();
}
