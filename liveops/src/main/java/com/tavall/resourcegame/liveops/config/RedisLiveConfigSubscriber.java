package com.tavall.resourcegame.liveops.config;

import com.tavall.resourcegame.dependency.IDependencyInjectableConcrete;
import com.tavall.resourcegame.liveops.ILiveOpsDomain;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public final class RedisLiveConfigSubscriber implements ILiveOpsDomain, IDependencyInjectableConcrete, AutoCloseable {
    private volatile JedisPubSub subscription;
    private volatile Future<?> future;

    public void start(ExecutorService executorService) {
        Objects.requireNonNull(executorService, "executorService");
        future = executorService.submit(() -> {
            try (Jedis jedis = getLiveOpsRedisPool().getResource()) {
                subscription = new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        handleMessage(message);
                    }
                };
                jedis.subscribe(subscription, getLiveOpsRedisConfig().liveConfigChannel());
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
            LiveConfigChange change = getLiveOpsObjectMapper().readValue(message, LiveConfigChange.class);
            getLiveConfigRegistry().apply(change.entry());
        } catch (Exception ex) {
            throw new LiveConfigValidationException("Failed to apply Redis live config change.", ex);
        }
    }
}
