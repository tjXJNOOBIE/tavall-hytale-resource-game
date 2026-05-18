package org.tavall.control.player.cache;

import org.tavall.control.cache.JacksonCacheCodec;
import org.tavall.control.cache.SemanticCacheFactory;
import org.tavall.control.config.CacheConfig;
import org.tavall.control.domain.PlayerProfile;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.tavall.abstractcache.cache.enums.CacheDomain;
import org.tavall.abstractcache.cache.enums.CacheSource;
import org.tavall.abstractcache.cache.enums.CacheVersion;
import org.tavall.abstractcache.cache.interfaces.ICacheValue;
import org.tavall.abstractcache.semantic.SemanticCache;
import org.tavall.abstractcache.semantic.model.CacheTag;
import org.tavall.abstractcache.semantic.model.SemanticCacheKey;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class PlayerProfileCache {
    private static final Duration PROFILE_TTL = Duration.ofMinutes(30);

    private final SemanticCache cache;
    private final JacksonCacheCodec<PlayerProfile> codec;

    public PlayerProfileCache(SemanticCache cache, JacksonCacheCodec<PlayerProfile> codec) {
        this.cache = cache;
        this.codec = codec;
    }

    public static PlayerProfileCache open(CacheConfig cacheConfig, ObjectMapper objectMapper) {
        return new PlayerProfileCache(
                new SemanticCacheFactory(cacheConfig).build("resource-game-profile"),
                new JacksonCacheCodec<>(objectMapper, PlayerProfile.class, "player-profile")
        );
    }

    public Optional<PlayerProfile> read(UUID playerId) {
        return cache.get(key(playerId), codec).map(ICacheValue::getValue);
    }

    public void write(UUID playerId, PlayerProfile profile) {
        cache.put(key(playerId), profile, PROFILE_TTL, codec);
    }

    public void prime(UUID playerId, PlayerProfile profile) {
        write(playerId, profile);
    }

    public boolean invalidate(UUID playerId) {
        return cache.invalidate(key(playerId));
    }

    static SemanticCacheKey key(UUID playerId) {
        return new SemanticCacheKey(
                "player-profile:" + playerId,
                CacheDomain.PLAYER_PROFILE,
                CacheSource.GLOBAL,
                CacheVersion.V1_0,
                Set.of(CacheTag.of("profile"))
        );
    }
}
