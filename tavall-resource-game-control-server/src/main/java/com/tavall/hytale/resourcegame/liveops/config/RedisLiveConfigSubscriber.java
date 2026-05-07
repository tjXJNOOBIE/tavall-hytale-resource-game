package com.tavall.hytale.resourcegame.liveops.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public final class RedisLiveConfigSubscriber implements AutoCloseable {
    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper;
    private final String channel;
    private final LiveConfigRegistry registry;
    private volatile JedisPubSub subscription;
    private volatile Future<?> future;

    public RedisLiveConfigSubscriber(JedisPool jedisPool, ObjectMapper objectMapper, String channel, LiveConfigRegistry registry) {
        this.jedisPool = Objects.requireNonNull(jedisPool, "jedisPool");
        this.objectMapper = objectMapper == null ? new ObjectMapper().findAndRegisterModules() : objectMapper;
        this.channel = channel == null || channel.isBlank() ? "resource-game:live-config:update" : channel;
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    public void start(ExecutorService executorService) {
        Objects.requireNonNull(executorService, "executorService");
        future = executorService.submit(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                subscription = new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        handleMessage(message);
                    }
                };
                jedis.subscribe(subscription, channel);
            }
        });
    }

    @Override
    public void close() {
        JedisPubSub currentSubscription = subscription;
        if (currentSubscription != null) {
            currentSubscription.unsubscribe();
        }
        Future<?> currentFuture = future;
        if (currentFuture != null) {
            currentFuture.cancel(true);
        }
    }

    private void handleMessage(String message) {
        try {
            LiveConfigChange change = objectMapper.readValue(message, LiveConfigChange.class);
            registry.apply(change.entry());
        } catch (Exception ex) {
            throw new LiveConfigValidationException("Failed to apply Redis live config change.", ex);
        }
    }
}
