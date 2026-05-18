package org.tavall.control.player.cache;

import org.tavall.control.cache.JacksonCacheCodec;
import org.tavall.control.cache.SemanticCacheFactory;
import org.tavall.control.config.CacheConfig;
import org.tavall.control.domain.PlayerGameState;
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

public final class PlayerGameStateCache {
    private static final Duration STATE_TTL = Duration.ofMinutes(15);

    private final SemanticCache cache;
    private final JacksonCacheCodec<PlayerGameState> codec;

    public PlayerGameStateCache(SemanticCache cache, JacksonCacheCodec<PlayerGameState> codec) {
        this.cache = cache;
        this.codec = codec;
    }

    public static PlayerGameStateCache open(CacheConfig cacheConfig, ObjectMapper objectMapper) {
        return new PlayerGameStateCache(
                new SemanticCacheFactory(cacheConfig).build("resource-game-game-state"),
                new JacksonCacheCodec<>(objectMapper, PlayerGameState.class, "player-game-state")
        );
    }

    public Optional<PlayerGameState> read(UUID playerId) {
        return cache.get(key(playerId), codec).map(ICacheValue::getValue);
    }

    public void write(UUID playerId, PlayerGameState state) {
        cache.put(key(playerId), state, STATE_TTL, codec);
    }

    public void prime(UUID playerId, PlayerGameState state) {
        write(playerId, state);
    }

    public boolean invalidate(UUID playerId) {
        return cache.invalidate(key(playerId));
    }

    static SemanticCacheKey key(UUID playerId) {
        return new SemanticCacheKey(
                "player-game-state:" + playerId,
                CacheDomain.KINGDOMS,
                CacheSource.GLOBAL,
                CacheVersion.V1_0,
                Set.of(CacheTag.of("game-state"))
        );
    }
}
