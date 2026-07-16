package org.tavall.api.minecraft.guild;

import org.tavall.dependency.IDependencyInjectableConcrete;
import org.tavall.dependency.annotations.DelegatesToInterface;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

@DelegatesToInterface(getLinkedInterface = IGuildBankDataBuilder.class)
public final class GuildBankDataBuilder implements IGuildBankDataBuilder, IDependencyInjectableConcrete {
    private final GuildId guildId;
    private final long coinBalance;
    private final Map<GuildResourceType, Integer> resourceBalances;
    private final Instant updatedAt;
    private final Map<String, String> metadata;

    public GuildBankDataBuilder() {
        this(null, 0L, Map.of(), Instant.now(), Map.of());
    }

    private GuildBankDataBuilder(
            GuildId guildId,
            long coinBalance,
            Map<GuildResourceType, Integer> resourceBalances,
            Instant updatedAt,
            Map<String, String> metadata
    ) {
        this.guildId = guildId;
        this.coinBalance = coinBalance;
        this.resourceBalances = resourceBalances == null ? Map.of() : Map.copyOf(resourceBalances);
        this.updatedAt = updatedAt == null ? Instant.now() : updatedAt;
        this.metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    @Override
    public IGuildBankDataBuilder guildId(GuildId guildId) {
        return new GuildBankDataBuilder(guildId, coinBalance, resourceBalances, updatedAt, metadata);
    }

    @Override
    public IGuildBankDataBuilder coinBalance(long coinBalance) {
        return new GuildBankDataBuilder(guildId, coinBalance, resourceBalances, updatedAt, metadata);
    }

    @Override
    public IGuildBankDataBuilder resourceBalances(Map<GuildResourceType, Integer> resourceBalances) {
        return new GuildBankDataBuilder(guildId, coinBalance, resourceBalances, updatedAt, metadata);
    }

    @Override
    public IGuildBankDataBuilder updatedAt(Instant updatedAt) {
        return new GuildBankDataBuilder(guildId, coinBalance, resourceBalances, updatedAt, metadata);
    }

    @Override
    public IGuildBankDataBuilder metadata(Map<String, String> metadata) {
        return new GuildBankDataBuilder(guildId, coinBalance, resourceBalances, updatedAt, metadata);
    }

    @Override
    public IGuildBankDataBuilder copyOf(GuildBankData guildBankData) {
        Objects.requireNonNull(guildBankData, "guildBankData");
        return new GuildBankDataBuilder(
                guildBankData.guildId(),
                guildBankData.coinBalance(),
                guildBankData.resourceBalances(),
                guildBankData.updatedAt(),
                guildBankData.metadata()
        );
    }

    @Override
    public GuildBankData build() {
        return new GuildBankData(guildId, coinBalance, resourceBalances, updatedAt, metadata);
    }
}
