package org.tavall.control.liveops.gui;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.liveops.LiveOpsDomain;
import org.tavall.control.liveops.config.LiveConfigValidationException;
import redis.clients.jedis.Jedis;

public final class RedisGlobalGuiPublisher implements GlobalGuiChangePublisher, LiveOpsDomain, IDependencyInjectableConcrete {
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
