package com.tavall.resourcegame.events.middleware;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import com.tavall.resourcegame.events.IGameEventDomain;
import com.tavall.resourcegame.events.core.GameEvent;
import redis.clients.jedis.Jedis;

public final class RedisGameEventForwarder implements DistributedEventForwarder, IGameEventDomain, IDependencyInjectableConcrete {
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
