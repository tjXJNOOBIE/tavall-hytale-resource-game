package org.tavall.api.minecraft.guild;

import org.tavall.dependency.IDependencyInjectableInterface;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public interface IGuildBankTransactionBuilder extends IDependencyInjectableInterface {
    IGuildBankTransactionBuilder transactionId(UUID transactionId);

    IGuildBankTransactionBuilder guildId(GuildId guildId);

    IGuildBankTransactionBuilder actorUniversalPlayerId(UUID actorUniversalPlayerId);

    IGuildBankTransactionBuilder transactionType(String transactionType);

    IGuildBankTransactionBuilder coinDelta(long coinDelta);

    IGuildBankTransactionBuilder resourceDelta(Map<GuildResourceType, Integer> resourceDelta);

    IGuildBankTransactionBuilder reason(String reason);

    IGuildBankTransactionBuilder createdAt(Instant createdAt);

    IGuildBankTransactionBuilder metadata(Map<String, String> metadata);

    IGuildBankTransactionBuilder copyOf(GuildBankTransaction guildBankTransaction);

    GuildBankTransaction build();
}
