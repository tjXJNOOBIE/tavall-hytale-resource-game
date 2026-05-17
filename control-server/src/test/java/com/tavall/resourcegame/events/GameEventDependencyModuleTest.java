package com.tavall.resourcegame.events;

import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import com.tavall.resourcegame.events.dispatch.RedisGameEventSubscriber;
import com.tavall.resourcegame.events.middleware.DistributedEventForwarder;
import com.tavall.resourcegame.events.middleware.NoopDistributedEventForwarder;
import com.tavall.resourcegame.events.middleware.RedisGameEventForwarder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.JedisPool;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class GameEventDependencyModuleTest {
    @AfterEach
    void tearDown() {
        DependencyLoaderAccess.findOptionalInstance(JedisPool.class).ifPresent(JedisPool::close);
        DependencyLoaderAccess.clear();
    }

    @Test
    void defaultRuntimeKeepsDistributedForwardingLocalOnly() {
        DependencyLoaderAccess.clear();

        new GameEventDependencyModule().registerDependencies();

        assertInstanceOf(
                NoopDistributedEventForwarder.class,
                DependencyLoaderAccess.findInstance(DistributedEventForwarder.class)
        );
    }

    @Test
    void redisConfiguredRuntimeUsesRedisForwarderAndSubscriber() {
        DependencyLoaderAccess.clear();
        DependencyLoaderAccess.registerInstance(
                GameEventRedisConfig.class,
                new GameEventRedisConfig("resource-game:test-events", "redis://127.0.0.1:6379", true)
        );

        new GameEventDependencyModule().registerDependencies();

        assertInstanceOf(
                RedisGameEventForwarder.class,
                DependencyLoaderAccess.findInstance(DistributedEventForwarder.class)
        );
        assertTrue(DependencyLoaderAccess.findOptionalInstance(JedisPool.class).isPresent());
        assertTrue(DependencyLoaderAccess.findOptionalInstance(RedisGameEventSubscriber.class).isPresent());
    }
}
