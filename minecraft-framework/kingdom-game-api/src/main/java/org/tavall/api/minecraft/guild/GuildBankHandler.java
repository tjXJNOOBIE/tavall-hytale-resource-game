package org.tavall.api.minecraft.guild;

import org.tavall.dependency.IDependencyInjectableConcrete;
import org.tavall.dependency.annotations.DelegatesToInterface;
import org.tavall.dependency.composition.IDependencyBundleAccess;

import java.time.Instant;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@DelegatesToInterface(getLinkedInterface = IGuildBankHandler.class)
public final class GuildBankHandler implements IGuildBankHandler, IDependencyInjectableConcrete, IDependencyBundleAccess<IGuildDependencies> {
    public IGuildDependencies dependencies() {
        return getDependencies();
    }

    private IGuildStateStore guildStateStore() {
        return dependencies().guildStateStore();
    }

    private IGuildMetaDataBuilder guildMetaDataBuilder() {
        return dependencies().guildMetaDataBuilder();
    }

    private IGuildBankDataBuilder guildBankDataBuilder() {
        return dependencies().guildBankDataBuilder();
    }

    @Override
    public GuildBankData getBank(GuildId guildId) {
        Objects.requireNonNull(guildId, "guildId");
        return guildStateStore().requireByGuildId(guildId).bank();
    }

    @Override
    public GuildBankTransaction recordTransaction(GuildBankTransaction transaction) {
        Objects.requireNonNull(transaction, "transaction");

        GuildMetaData guild = guildStateStore().requireByGuildId(transaction.guildId());
        GuildBankData currentBank = guild.bank();

        long coinBalance = Math.addExact(currentBank.coinBalance(), transaction.coinDelta());
        if (coinBalance < 0L) {
            throw new IllegalStateException("Guild bank cannot go negative.");
        }

        Map<GuildResourceType, Integer> resourceBalances = new EnumMap<>(GuildResourceType.class);
        resourceBalances.putAll(currentBank.resourceBalances());
        for (Map.Entry<GuildResourceType, Integer> entry : transaction.resourceDelta().entrySet()) {
            int updatedAmount = Math.addExact(resourceBalances.getOrDefault(entry.getKey(), 0), entry.getValue());
            if (updatedAmount < 0) {
                throw new IllegalStateException("Guild resource balance cannot go negative for " + entry.getKey());
            }
            resourceBalances.put(entry.getKey(), updatedAmount);
        }

        Map<String, String> metadata = new HashMap<>(currentBank.metadata());
        metadata.put("lastTransactionId", transaction.transactionId().toString());
        metadata.put("lastTransactionType", transaction.transactionType());
        metadata.put("lastTransactionReason", transaction.reason());

        Instant now = transaction.createdAt();
        GuildBankData updatedBank = guildBankDataBuilder()
                .copyOf(currentBank)
                .coinBalance(coinBalance)
                .resourceBalances(resourceBalances)
                .updatedAt(now)
                .metadata(metadata)
                .build();
        guildStateStore().save(
                guildMetaDataBuilder()
                        .copyOf(guild)
                        .bank(updatedBank)
                        .updatedAt(now)
                        .build()
        );
        return transaction;
    }
}


