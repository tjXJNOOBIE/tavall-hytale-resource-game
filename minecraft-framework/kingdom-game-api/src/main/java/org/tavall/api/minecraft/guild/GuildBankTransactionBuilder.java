package org.tavall.api.minecraft.guild;

import org.tavall.dependency.IDependencyInjectableConcrete;
import org.tavall.dependency.annotations.DelegatesToInterface;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@DelegatesToInterface(getLinkedInterface = IGuildBankTransactionBuilder.class)
public final class GuildBankTransactionBuilder implements IGuildBankTransactionBuilder, IDependencyInjectableConcrete {
    private final UUID transactionId;
    private final GuildId guildId;
    private final UUID actorUniversalPlayerId;
    private final String transactionType;
    private final long coinDelta;
    private final Map<GuildResourceType, Integer> resourceDelta;
    private final String reason;
    private final Instant createdAt;
    private final Map<String, String> metadata;

    public GuildBankTransactionBuilder() {
        this(UUID.randomUUID(), null, null, "guild_bank_transaction", 0L, Map.of(), "guild bank transaction", Instant.now(), Map.of());
    }

    private GuildBankTransactionBuilder(
            UUID transactionId,
            GuildId guildId,
            UUID actorUniversalPlayerId,
            String transactionType,
            long coinDelta,
            Map<GuildResourceType, Integer> resourceDelta,
            String reason,
            Instant createdAt,
            Map<String, String> metadata
    ) {
        this.transactionId = transactionId;
        this.guildId = guildId;
        this.actorUniversalPlayerId = actorUniversalPlayerId;
        this.transactionType = transactionType;
        this.coinDelta = coinDelta;
        this.resourceDelta = resourceDelta == null ? Map.of() : Map.copyOf(resourceDelta);
        this.reason = reason;
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
        this.metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    @Override
    public IGuildBankTransactionBuilder transactionId(UUID transactionId) {
        return new GuildBankTransactionBuilder(transactionId, guildId, actorUniversalPlayerId, transactionType, coinDelta, resourceDelta, reason, createdAt, metadata);
    }

    @Override
    public IGuildBankTransactionBuilder guildId(GuildId guildId) {
        return new GuildBankTransactionBuilder(transactionId, guildId, actorUniversalPlayerId, transactionType, coinDelta, resourceDelta, reason, createdAt, metadata);
    }

    @Override
    public IGuildBankTransactionBuilder actorUniversalPlayerId(UUID actorUniversalPlayerId) {
        return new GuildBankTransactionBuilder(transactionId, guildId, actorUniversalPlayerId, transactionType, coinDelta, resourceDelta, reason, createdAt, metadata);
    }

    @Override
    public IGuildBankTransactionBuilder transactionType(String transactionType) {
        return new GuildBankTransactionBuilder(transactionId, guildId, actorUniversalPlayerId, transactionType, coinDelta, resourceDelta, reason, createdAt, metadata);
    }

    @Override
    public IGuildBankTransactionBuilder coinDelta(long coinDelta) {
        return new GuildBankTransactionBuilder(transactionId, guildId, actorUniversalPlayerId, transactionType, coinDelta, resourceDelta, reason, createdAt, metadata);
    }

    @Override
    public IGuildBankTransactionBuilder resourceDelta(Map<GuildResourceType, Integer> resourceDelta) {
        return new GuildBankTransactionBuilder(transactionId, guildId, actorUniversalPlayerId, transactionType, coinDelta, resourceDelta, reason, createdAt, metadata);
    }

    @Override
    public IGuildBankTransactionBuilder reason(String reason) {
        return new GuildBankTransactionBuilder(transactionId, guildId, actorUniversalPlayerId, transactionType, coinDelta, resourceDelta, reason, createdAt, metadata);
    }

    @Override
    public IGuildBankTransactionBuilder createdAt(Instant createdAt) {
        return new GuildBankTransactionBuilder(transactionId, guildId, actorUniversalPlayerId, transactionType, coinDelta, resourceDelta, reason, createdAt, metadata);
    }

    @Override
    public IGuildBankTransactionBuilder metadata(Map<String, String> metadata) {
        return new GuildBankTransactionBuilder(transactionId, guildId, actorUniversalPlayerId, transactionType, coinDelta, resourceDelta, reason, createdAt, metadata);
    }

    @Override
    public IGuildBankTransactionBuilder copyOf(GuildBankTransaction guildBankTransaction) {
        Objects.requireNonNull(guildBankTransaction, "guildBankTransaction");
        return new GuildBankTransactionBuilder(
                guildBankTransaction.transactionId(),
                guildBankTransaction.guildId(),
                guildBankTransaction.actorUniversalPlayerId(),
                guildBankTransaction.transactionType(),
                guildBankTransaction.coinDelta(),
                guildBankTransaction.resourceDelta(),
                guildBankTransaction.reason(),
                guildBankTransaction.createdAt(),
                guildBankTransaction.metadata()
        );
    }

    @Override
    public GuildBankTransaction build() {
        return new GuildBankTransaction(transactionId, guildId, actorUniversalPlayerId, transactionType, coinDelta, resourceDelta, reason, createdAt, metadata);
    }
}
