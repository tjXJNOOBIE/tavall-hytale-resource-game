package com.tavall.hytale.resourcegame.events.dispatch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tavall.hytale.resourcegame.events.core.BasicGameEvent;
import com.tavall.hytale.resourcegame.events.core.EventSource;
import com.tavall.hytale.resourcegame.events.middleware.RedisGameEventEnvelope;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public final class RedisGameEventSubscriber implements AutoCloseable {
    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper;
    private final String channel;
    private final GameEventDispatchHandler dispatchHandler;
    private volatile JedisPubSub subscription;
    private volatile Future<?> future;

    public RedisGameEventSubscriber(JedisPool jedisPool, ObjectMapper objectMapper, String channel, GameEventDispatchHandler dispatchHandler) {
        this.jedisPool = Objects.requireNonNull(jedisPool, "jedisPool");
        this.objectMapper = objectMapper == null ? new ObjectMapper().findAndRegisterModules() : objectMapper;
        this.channel = channel == null || channel.isBlank() ? "resource-game:events" : channel;
        this.dispatchHandler = Objects.requireNonNull(dispatchHandler, "dispatchHandler");
    }

    public void start(ExecutorService executorService) {
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
            RedisGameEventEnvelope envelope = objectMapper.readValue(message, RedisGameEventEnvelope.class);
            dispatchHandler.dispatch(new BasicGameEvent(
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
