package org.tavall.control.liveops.gui;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.liveops.ILiveOpsDomain;
import org.tavall.control.liveops.config.LiveConfigValidationException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public final class RedisGlobalGuiSubscriber implements ILiveOpsDomain, IDependencyInjectableConcrete, AutoCloseable {
    private volatile JedisPubSub subscription;
    private volatile Future<?> future;

    public void start(ExecutorService executorService) {
        future = executorService.submit(() -> {
            try (Jedis jedis = getLiveOpsRedisPool().getResource()) {
                subscription = new JedisPubSub() {
                    @Override
                    public void onPMessage(String pattern, String channel, String message) {
                        handleMessage(message);
                    }
                };
                jedis.psubscribe(subscription, getLiveOpsRedisConfig().globalGuiChannelPattern());
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
            GlobalGuiChange change = getLiveOpsObjectMapper().readValue(message, GlobalGuiChange.class);
            getGlobalGuiRegistry().apply(change.definition());
        } catch (Exception ex) {
            throw new LiveConfigValidationException("Failed to apply Redis global GUI change.", ex);
        }
    }
}
