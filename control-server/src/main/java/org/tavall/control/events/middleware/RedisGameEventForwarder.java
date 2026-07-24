package org.tavall.control.events.middleware;

import org.tavall.dependency.IDependencyInjectableConcrete;
import org.tavall.control.events.GameEventDomain;
import org.tavall.control.events.core.GameEvent;
import redis.clients.jedis.Jedis;

public final class RedisGameEventForwarder implements DistributedEventForwarder, GameEventDomain, IDependencyInjectableConcrete {
    @Override
    public void forward(GameEvent event) {
        try (Jedis jedis = getGameEventRedisPool().getResource()) {
            jedis.publish(
                    getGameEventRedisConfig().channel(),
                    getGameEventObjectMapper().writeValueAsString(RedisGameEventEnvelope.from(event))
            );
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to forward backend game event to Redis.", ex);
        }
    }
}
