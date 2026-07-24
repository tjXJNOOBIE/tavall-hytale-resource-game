package org.tavall.control.liveops.config;

import org.tavall.dependency.IDependencyInjectableConcrete;
import org.tavall.control.liveops.LiveOpsDomain;
import redis.clients.jedis.Jedis;

public final class RedisLiveConfigPublisher implements LiveConfigChangePublisher, LiveOpsDomain, IDependencyInjectableConcrete {
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
