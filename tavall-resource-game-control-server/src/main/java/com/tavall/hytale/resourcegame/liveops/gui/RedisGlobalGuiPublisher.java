package com.tavall.hytale.resourcegame.liveops.gui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tavall.hytale.resourcegame.liveops.config.LiveConfigValidationException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Objects;

public final class RedisGlobalGuiPublisher implements GlobalGuiChangePublisher {
    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper;
    private final String channelPrefix;

    public RedisGlobalGuiPublisher(JedisPool jedisPool, ObjectMapper objectMapper, String channelPrefix) {
        this.jedisPool = Objects.requireNonNull(jedisPool, "jedisPool");
        this.objectMapper = objectMapper == null ? new ObjectMapper().findAndRegisterModules() : objectMapper;
        this.channelPrefix = channelPrefix == null || channelPrefix.isBlank() ? "resource-game:gui:update:" : channelPrefix;
    }

    @Override
    public void publish(GlobalGuiChange change) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.publish(channelPrefix + change.definition().guiKey(), objectMapper.writeValueAsString(change));
        } catch (Exception ex) {
            throw new LiveConfigValidationException("Failed to publish global GUI change to Redis.", ex);
        }
    }
}
