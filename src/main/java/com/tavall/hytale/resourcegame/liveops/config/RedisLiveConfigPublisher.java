package com.tavall.hytale.resourcegame.liveops.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Objects;

public final class RedisLiveConfigPublisher implements LiveConfigChangePublisher {
    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper;
    private final String channel;

    public RedisLiveConfigPublisher(JedisPool jedisPool, ObjectMapper objectMapper, String channel) {
        this.jedisPool = Objects.requireNonNull(jedisPool, "jedisPool");
        this.objectMapper = objectMapper == null ? new ObjectMapper().findAndRegisterModules() : objectMapper;
        this.channel = channel == null || channel.isBlank() ? "resource-game:live-config:update" : channel;
    }

    @Override
    public void publish(LiveConfigChange change) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.publish(channel, objectMapper.writeValueAsString(change));
        } catch (Exception ex) {
            throw new LiveConfigValidationException("Failed to publish live config change to Redis.", ex);
        }
    }
}
