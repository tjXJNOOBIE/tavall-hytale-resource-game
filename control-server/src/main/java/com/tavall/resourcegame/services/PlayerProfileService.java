package com.tavall.resourcegame.services;

import com.tavall.resourcegame.cache.JacksonCacheCodec;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import com.tavall.resourcegame.dependency.interfaces.IPlayerProfileService;
import com.tavall.resourcegame.domain.PlayerProfile;
import com.tavall.resourcegame.player.cache.PlayerProfileCache;
import com.tavall.resourcegame.persistence.PlayerProfileStore;
import org.tavall.abstractcache.semantic.SemanticCache;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Loads and caches player profile data.
 */
public final class PlayerProfileService implements IPlayerProfileService, IDependencyInjectableConcrete {
    private static final Logger LOGGER = Logger.getLogger(PlayerProfileService.class.getName());

    private final PlayerProfileStore repository;
    private final PlayerProfileCache cache;
    private final InfrastructureMetricsRecorder metricsRecorder;

    public PlayerProfileService(PlayerProfileStore repository, SemanticCache cache, JacksonCacheCodec<PlayerProfile> codec) {
        this(repository, new PlayerProfileCache(cache, codec), InfrastructureMetricsRecorder.defaultRecorder());
    }

    public PlayerProfileService(
            PlayerProfileStore repository,
            SemanticCache cache,
            JacksonCacheCodec<PlayerProfile> codec,
            InfrastructureMetricsRecorder metricsRecorder
    ) {
        this(repository, new PlayerProfileCache(cache, codec), metricsRecorder);
    }

    public PlayerProfileService(
            PlayerProfileStore repository,
            PlayerProfileCache cache,
            InfrastructureMetricsRecorder metricsRecorder
    ) {
        this.repository = repository;
        this.cache = cache;
        this.metricsRecorder = metricsRecorder;
    }

    public Optional<PlayerProfile> readCached(UUID playerId) {
        long startedAtNanos = System.nanoTime();
        try {
            Optional<PlayerProfile> cached = cache.read(playerId);
            metricsRecorder.recordProfileCacheRead(cached.isPresent(), true, System.nanoTime() - startedAtNanos);
            return cached;
        } catch (Exception ex) {
            metricsRecorder.recordProfileCacheRead(false, false, System.nanoTime() - startedAtNanos);
            LOGGER.warning(() -> "Player profile cache read failed for " + playerId + ". Falling back to persistence. " + ex.getMessage());
            return Optional.empty();
        }
    }

    public PlayerProfile loadOrCreate(UUID playerId, String name, String timezone, String ipHash, Instant now) {
        Optional<PlayerProfile> cached = readCached(playerId);
        if (cached.isPresent()) {
            LOGGER.info(() -> "Player profile cache hit for " + playerId + ".");
            return cached.get();
        }

        try {
            Optional<PlayerProfile> existing = findByUuid(playerId);
            if (existing.isPresent()) {
                LOGGER.info(() -> "Player profile repository hit for " + playerId + ".");
            } else {
                LOGGER.info(() -> "Creating new player profile for " + playerId + ".");
            }
            if (existing.isEmpty()) {
                try {
                    upsertProfile(playerId, name, timezone, ipHash, now);
                } catch (Exception ex) {
                    throw new IllegalStateException("Failed to create player profile", ex);
                }
            }
            PlayerProfile refreshed = upsertProfile(playerId, name, timezone, ipHash, now);
            cacheProfile(playerId, refreshed);
            return refreshed;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load player profile", ex);
        }
    }

    public void persist(PlayerProfile profile, Instant now) {
        try {
            PlayerProfile refreshed = upsertProfile(profile.uuid(), profile.name(), profile.timezone(), profile.ipHash(), now);
            cacheProfile(profile.uuid(), refreshed);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to persist profile", ex);
        }
    }

    private void cacheProfile(UUID playerId, PlayerProfile profile) {
        long startedAtNanos = System.nanoTime();
        try {
            cache.write(playerId, profile);
            metricsRecorder.recordCacheWrite(true, System.nanoTime() - startedAtNanos);
        } catch (Exception ex) {
            metricsRecorder.recordCacheWrite(false, System.nanoTime() - startedAtNanos);
            LOGGER.warning(() -> "Player profile cache write failed for " + playerId + ". " + ex.getMessage());
        }
    }

    private Optional<PlayerProfile> findByUuid(UUID playerId) throws Exception {
        long startedAtNanos = System.nanoTime();
        try {
            Optional<PlayerProfile> existing = repository.findByUuid(playerId);
            metricsRecorder.recordProfileStoreRead(true, System.nanoTime() - startedAtNanos);
            return existing;
        } catch (Exception ex) {
            metricsRecorder.recordProfileStoreRead(false, System.nanoTime() - startedAtNanos);
            throw ex;
        }
    }

    private PlayerProfile upsertProfile(UUID playerId, String name, String timezone, String ipHash, Instant now) throws Exception {
        long startedAtNanos = System.nanoTime();
        try {
            PlayerProfile profile = repository.upsert(playerId, name, timezone, ipHash, now);
            metricsRecorder.recordProfileSave(true, System.nanoTime() - startedAtNanos);
            return profile;
        } catch (Exception ex) {
            metricsRecorder.recordProfileSave(false, System.nanoTime() - startedAtNanos);
            throw ex;
        }
    }
}
