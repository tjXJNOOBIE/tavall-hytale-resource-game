package com.tavall.resourcegame.liveops.config;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import com.tavall.resourcegame.liveops.ILiveOpsDomain;
import redis.clients.jedis.Jedis;

public final class RedisLiveConfigPublisher implements LiveConfigChangePublisher, ILiveOpsDomain, IDependencyInjectableConcrete {
    @Override
    public void publish(LiveConfigChange change) {
        try (Jedis jedis = getLiveOpsRedisPool().getResource()) {
            jedis.publish(
                    getLiveOpsRedisConfig().liveConfigChannel(),
                    getLiveOpsObjectMapper().writeValueAsString(change)
            );
        } catch (Exception ex) {
            throw new LiveConfigValidationException("Failed to publish live config change to Redis.", ex);
        }
    }
}
