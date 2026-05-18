package org.tavall.control.liveops.config;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.liveops.ILiveOpsDomain;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public final class RedisLiveConfigSubscriber implements ILiveOpsDomain, IDependencyInjectableConcrete, AutoCloseable {
    private volatile JedisPubSub subscription;
    private volatile Future<?> future;

    public void start(ExecutorService executorHandler) {
        Objects.requireNonNull(executorHandler, "executorHandler");
        future = executorHandler.submit(() -> {
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
