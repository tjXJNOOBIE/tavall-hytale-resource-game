package org.tavall.control.events;

import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import org.tavall.control.events.dispatch.RedisGameEventSubscriber;
import org.tavall.control.events.middleware.DistributedEventForwarder;
import org.tavall.control.events.middleware.NoopDistributedEventForwarder;
import org.tavall.control.events.middleware.RedisGameEventForwarder;
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
