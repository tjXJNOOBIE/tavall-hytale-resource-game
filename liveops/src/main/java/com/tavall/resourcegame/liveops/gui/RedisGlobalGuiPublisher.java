package com.tavall.resourcegame.liveops.gui;

import com.tavall.resourcegame.dependency.IDependencyInjectableConcrete;
import com.tavall.resourcegame.liveops.ILiveOpsDomain;
import com.tavall.resourcegame.liveops.config.LiveConfigValidationException;
import redis.clients.jedis.Jedis;

public final class RedisGlobalGuiPublisher implements GlobalGuiChangePublisher, ILiveOpsDomain, IDependencyInjectableConcrete {
    @Override
    public void publish(GlobalGuiChange change) {
        try (Jedis jedis = getLiveOpsRedisPool().getResource()) {
            jedis.publish(
                    getLiveOpsRedisConfig().globalGuiChannelPrefix() + change.definition().guiKey(),
                    getLiveOpsObjectMapper().writeValueAsString(change)
            );
        } catch (Exception ex) {
            throw new LiveConfigValidationException("Failed to publish global GUI change to Redis.", ex);
        }
    }
}
