package com.tavall.resourcegame.middleware.citizen.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tavall.resourcegame.cache.JacksonCacheCodec;
import com.tavall.resourcegame.cache.SemanticCacheFactory;
import com.tavall.resourcegame.config.CacheConfig;
import com.tavall.resourcegame.middleware.citizen.CitizenSummaryBundle;
import com.tavall.resourcegame.middleware.citizen.CitizenSummaryScope;
import org.tavall.abstractcache.cache.enums.CacheDomain;
import org.tavall.abstractcache.cache.enums.CacheSource;
import org.tavall.abstractcache.cache.enums.CacheVersion;
import org.tavall.abstractcache.cache.interfaces.ICacheValue;
import org.tavall.abstractcache.semantic.SemanticCache;
import org.tavall.abstractcache.semantic.model.CacheTag;
import org.tavall.abstractcache.semantic.model.SemanticCacheKey;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class CitizenSummaryCache {
    private static final Duration MEMORY_TTL = Duration.ofMinutes(5);
    private static final Duration SHARED_TTL = Duration.ofMinutes(20);

    private final SemanticCache memoryCache;
    private final SemanticCache sharedCache;
    private final JacksonCacheCodec<CitizenSummaryBundle> codec;
    private final Set<String> dirtySharedScopes;
    private final Map<String, CitizenSummaryBundle> hotMemorySummaries;

    public CitizenSummaryCache(SemanticCache memoryCache, SemanticCache sharedCache, ObjectMapper objectMapper) {
        this.memoryCache = memoryCache;
        this.sharedCache = sharedCache;
        this.codec = new JacksonCacheCodec<>(objectMapper, CitizenSummaryBundle.class, "citizen-summary");
        this.dirtySharedScopes = ConcurrentHashMap.newKeySet();
        this.hotMemorySummaries = new ConcurrentHashMap<>();
    }

    public static CitizenSummaryCache open(CacheConfig cacheConfig, ObjectMapper objectMapper) {
        return new CitizenSummaryCache(
                new SemanticCacheFactory(cacheConfig).build("citizen-summary-memory"),
                new SemanticCacheFactory(cacheConfig).build("citizen-summary-shared"),
                objectMapper
        );
    }

    public static CitizenSummaryCache openInMemory(String cacheSuffix, ObjectMapper objectMapper) {
        return new CitizenSummaryCache(
                new SemanticCacheFactory(new CacheConfig("", 6379, "", false)).build("citizen-summary-memory-" + cacheSuffix),
                new SemanticCacheFactory(new CacheConfig("", 6379, "", false)).build("citizen-summary-shared-" + cacheSuffix),
                objectMapper
        );
    }

    public Optional<CitizenSummaryBundle> readMemory(CitizenSummaryScope scope) {
        CitizenSummaryBundle hotValue = hotMemorySummaries.get(scope.cacheKey());
        if (hotValue != null) {
            return Optional.of(hotValue);
        }
        Optional<CitizenSummaryBundle> cached = memoryCache.get(key(scope), codec).map(ICacheValue::getValue);
        cached.ifPresent(value -> hotMemorySummaries.put(scope.cacheKey(), value));
        return cached;
    }

    public Optional<CitizenSummaryBundle> readShared(CitizenSummaryScope scope) {
        if (isSharedDirty(scope)) {
            return Optional.empty();
        }
        return sharedCache.get(key(scope), codec).map(ICacheValue::getValue);
    }

    public void writeMemory(CitizenSummaryScope scope, CitizenSummaryBundle summary) {
        hotMemorySummaries.put(scope.cacheKey(), summary);
        memoryCache.put(key(scope), summary, MEMORY_TTL, codec);
    }

    public void primeMemory(CitizenSummaryScope scope, CitizenSummaryBundle summary) {
        writeMemory(scope, summary);
    }

    public void writeShared(CitizenSummaryScope scope, CitizenSummaryBundle summary) {
        sharedCache.put(key(scope), summary, SHARED_TTL, codec);
        clearSharedDirty(scope);
    }

    public void primeShared(CitizenSummaryScope scope, CitizenSummaryBundle summary) {
        writeShared(scope, summary);
    }

    public void invalidateMemory(CitizenSummaryScope scope) {
        hotMemorySummaries.remove(scope.cacheKey());
        memoryCache.invalidate(key(scope));
    }

    public void markSharedDirty(CitizenSummaryScope scope) {
        dirtySharedScopes.add(scope.cacheKey());
    }

    public void clearSharedDirty(CitizenSummaryScope scope) {
        dirtySharedScopes.remove(scope.cacheKey());
    }

    public boolean isSharedDirty(CitizenSummaryScope scope) {
        return dirtySharedScopes.contains(scope.cacheKey());
    }

    private SemanticCacheKey key(CitizenSummaryScope scope) {
        return new SemanticCacheKey(
                "citizen-summary:" + scope.cacheKey(),
                CacheDomain.KINGDOMS,
                CacheSource.GLOBAL,
                CacheVersion.V1_0,
                Set.of(CacheTag.of("citizen-summary"), CacheTag.of(scope.scopeType().name().toLowerCase()))
        );
    }
}
