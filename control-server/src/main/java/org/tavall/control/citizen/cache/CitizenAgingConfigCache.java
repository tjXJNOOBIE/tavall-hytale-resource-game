package org.tavall.control.citizen.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.tavall.abstractcache.cache.enums.CacheDomain;
import org.tavall.abstractcache.cache.enums.CacheSource;
import org.tavall.abstractcache.cache.enums.CacheVersion;
import org.tavall.abstractcache.cache.interfaces.ICacheValue;
import org.tavall.abstractcache.semantic.SemanticCache;
import org.tavall.abstractcache.semantic.model.CacheTag;
import org.tavall.abstractcache.semantic.model.SemanticCacheKey;
import org.tavall.control.cache.JacksonCacheCodec;
import org.tavall.control.cache.SemanticCacheFactory;
import org.tavall.control.citizen.CitizenAgingConfig;
import org.tavall.control.config.CacheConfig;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

public final class CitizenAgingConfigCache {
    private static final Duration CONFIG_TTL = Duration.ofHours(12);

    private final SemanticCache cache;
    private final JacksonCacheCodec<CitizenAgingConfig> codec;

    public CitizenAgingConfigCache(SemanticCache cache, JacksonCacheCodec<CitizenAgingConfig> codec) {
        this.cache = cache;
        this.codec = codec;
    }

    public static CitizenAgingConfigCache open(CacheConfig cacheConfig, ObjectMapper objectMapper) {
        return new CitizenAgingConfigCache(
                new SemanticCacheFactory(cacheConfig).build("citizen-aging-config"),
                new JacksonCacheCodec<>(objectMapper, CitizenAgingConfig.class, "citizen-aging-config")
        );
    }

    public static CitizenAgingConfigCache openInMemory(String cacheSuffix, ObjectMapper objectMapper) {
        return new CitizenAgingConfigCache(
                new SemanticCacheFactory(new CacheConfig("", 6379, "", false)).build("citizen-aging-config-" + cacheSuffix),
                new JacksonCacheCodec<>(objectMapper, CitizenAgingConfig.class, "citizen-aging-config")
        );
    }

    public Optional<CitizenAgingConfig> read() {
        return cache.get(key(), codec).map(ICacheValue::getValue);
    }

    public void write(CitizenAgingConfig config) {
        cache.put(key(), config, CONFIG_TTL, codec);
    }

    public void prime(CitizenAgingConfig config) {
        write(config);
    }

    public boolean invalidate() {
        return cache.invalidate(key());
    }

    private SemanticCacheKey key() {
        return new SemanticCacheKey(
                CitizenAgingConfig.LIVE_CONFIG_KEY,
                CacheDomain.ECONOMY,
                CacheSource.GLOBAL,
                CacheVersion.V1_0,
                Set.of(CacheTag.of("citizen-aging-config"))
        );
    }
}
