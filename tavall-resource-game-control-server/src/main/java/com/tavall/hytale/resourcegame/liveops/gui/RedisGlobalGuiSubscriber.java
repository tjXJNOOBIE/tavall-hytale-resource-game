package com.tavall.hytale.resourcegame.liveops.gui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tavall.hytale.resourcegame.liveops.config.LiveConfigValidationException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public final class RedisGlobalGuiSubscriber implements AutoCloseable {
    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper;
    private final String channelPattern;
    private final GlobalGuiRegistry registry;
    private volatile JedisPubSub subscription;
    private volatile Future<?> future;

    public RedisGlobalGuiSubscriber(JedisPool jedisPool, ObjectMapper objectMapper, String channelPattern, GlobalGuiRegistry registry) {
        this.jedisPool = Objects.requireNonNull(jedisPool, "jedisPool");
        this.objectMapper = objectMapper == null ? new ObjectMapper().findAndRegisterModules() : objectMapper;
        this.channelPattern = channelPattern == null || channelPattern.isBlank() ? "resource-game:gui:update:*" : channelPattern;
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    public void start(ExecutorService executorService) {
        future = executorService.submit(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                subscription = new JedisPubSub() {
                    @Override
                    public void onPMessage(String pattern, String channel, String message) {
                        handleMessage(message);
                    }
                };
                jedis.psubscribe(subscription, channelPattern);
            }
        });
    }

    @Override
    public void close() {
        JedisPubSub currentSubscription = subscription;
        if (currentSubscription != null) {
            currentSubscription.punsubscribe();
        }
        Future<?> currentFuture = future;
        if (currentFuture != null) {
            currentFuture.cancel(true);
        }
    }

    private void handleMessage(String message) {
        try {
            GlobalGuiChange change = objectMapper.readValue(message, GlobalGuiChange.class);
            registry.apply(change.definition());
        } catch (Exception ex) {
            throw new LiveConfigValidationException("Failed to apply Redis global GUI change.", ex);
        }
    }
}
