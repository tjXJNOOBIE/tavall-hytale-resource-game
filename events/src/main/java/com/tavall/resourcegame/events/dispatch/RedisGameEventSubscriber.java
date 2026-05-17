package com.tavall.resourcegame.events.dispatch;

import com.tavall.resourcegame.dependency.IDependencyInjectableConcrete;
import com.tavall.resourcegame.events.IGameEventDomain;
import com.tavall.resourcegame.events.core.BasicGameEvent;
import com.tavall.resourcegame.events.core.EventSource;
import com.tavall.resourcegame.events.middleware.RedisGameEventEnvelope;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public final class RedisGameEventSubscriber implements IGameEventDomain, IDependencyInjectableConcrete, AutoCloseable {
    private volatile JedisPubSub subscription;
    private volatile Future<?> future;

    public void start(ExecutorService executorService) {
        future = executorService.submit(() -> {
            try (Jedis jedis = getGameEventRedisPool().getResource()) {
                subscription = new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        handleMessage(message);
                    }
                };
                jedis.subscribe(subscription, getGameEventRedisConfig().channel());
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
            RedisGameEventEnvelope envelope = getGameEventObjectMapper().readValue(message, RedisGameEventEnvelope.class);
            getGameEventDispatchHandler().dispatch(new BasicGameEvent(
                    envelope.eventId(),
                    envelope.eventType(),
                    envelope.actorId(),
                    envelope.createdAtEpochMillis(),
                    EventSource.REMOTE_NODE,
                    envelope.attributes()
            ));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to dispatch Redis backend game event.", ex);
        }
    }
}
