package com.tavall.hytale.resourcegame.events.middleware;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tavall.hytale.resourcegame.events.core.GameEvent;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Objects;

public final class RedisGameEventForwarder implements DistributedEventForwarder {
    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper;
    private final String channel;

    public RedisGameEventForwarder(JedisPool jedisPool, ObjectMapper objectMapper, String channel) {
        this.jedisPool = Objects.requireNonNull(jedisPool, "jedisPool");
        this.objectMapper = objectMapper == null ? new ObjectMapper().findAndRegisterModules() : objectMapper;
        this.channel = channel == null || channel.isBlank() ? "resource-game:events" : channel;
    }

    @Override
    public void forward(GameEvent event) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.publish(channel, objectMapper.writeValueAsString(RedisGameEventEnvelope.from(event)));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to forward backend game event to Redis.", ex);
        }
    }
}
