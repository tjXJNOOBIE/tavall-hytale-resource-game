package org.tavall.control.citizen;

import org.tavall.control.citizen.cache.CitizenSummaryCache;
import org.tavall.control.transport.JsonMapperProvider;
import org.tavall.dependency.IDependencyInjectableConcrete;

import java.util.Optional;
import java.util.UUID;

public final class InMemoryCitizenSummaryCacheRepository implements CitizenSummaryCacheRepository, IDependencyInjectableConcrete {
    private final CitizenSummaryCache cache;

    public InMemoryCitizenSummaryCacheRepository() {
        this(createDefaultCache());
    }

    public InMemoryCitizenSummaryCacheRepository(CitizenSummaryCache cache) {
        this.cache = cache;
    }

    private static CitizenSummaryCache createDefaultCache() {
        String cacheSuffix = UUID.randomUUID().toString();
        return CitizenSummaryCache.openInMemory(cacheSuffix, new JsonMapperProvider().mapper());
    }

    @Override
    public Optional<CitizenSummaryBundle> findMemorySummary(CitizenSummaryScope scope) {
        return cache.readMemory(scope);
    }

    @Override
    public Optional<CitizenSummaryBundle> findRedisSummary(CitizenSummaryScope scope) {
        return cache.readShared(scope);
    }

    @Override
    public void saveMemorySummary(CitizenSummaryScope scope, CitizenSummaryBundle summary) {
        cache.writeMemory(scope, summary);
    }

    @Override
    public void saveRedisSummary(CitizenSummaryScope scope, CitizenSummaryBundle summary) {
        cache.writeShared(scope, summary);
    }

    @Override
    public void invalidateMemorySummary(CitizenSummaryScope scope) {
        cache.invalidateMemory(scope);
    }

    @Override
    public void markRedisDirty(CitizenSummaryScope scope) {
        cache.markSharedDirty(scope);
    }

    @Override
    public void clearRedisDirty(CitizenSummaryScope scope) {
        cache.clearSharedDirty(scope);
    }

    @Override
    public boolean redisDirty(CitizenSummaryScope scope) {
        return cache.isSharedDirty(scope);
    }
}

