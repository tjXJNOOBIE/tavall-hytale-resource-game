package org.tavall.control.events.dispatch;

import org.tavall.dependency.IDependencyInjectableConcrete;
import org.tavall.control.events.GameEventDomain;
import org.tavall.control.events.core.BasicGameEvent;
import org.tavall.control.events.core.EventSource;
import org.tavall.control.events.middleware.RedisGameEventEnvelope;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public final class RedisGameEventSubscriber implements GameEventDomain, IDependencyInjectableConcrete, AutoCloseable {
    private volatile JedisPubSub subscription;
    private volatile Future<?> future;

    public void start(ExecutorService executorHandler) {
        future = executorHandler.submit(() -> {
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
