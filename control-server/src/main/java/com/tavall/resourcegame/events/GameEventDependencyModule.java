package com.tavall.resourcegame.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import com.tjxjnoobie.api.dependency.IDependencyModule;
import com.tavall.resourcegame.events.dispatch.EventInteractionMappingRegistry;
import com.tavall.resourcegame.events.dispatch.GameEventDispatchHandler;
import com.tavall.resourcegame.events.dispatch.GameEventListenerRegistry;
import com.tavall.resourcegame.events.dispatch.InteractionBridgeHandler;
import com.tavall.resourcegame.events.dispatch.RedisGameEventSubscriber;
import com.tavall.resourcegame.events.middleware.AllowAllEventPermissionPolicy;
import com.tavall.resourcegame.events.middleware.DistributedEventForwarder;
import com.tavall.resourcegame.events.middleware.EventAuditMiddleware;
import com.tavall.resourcegame.events.middleware.EventCancellationMiddleware;
import com.tavall.resourcegame.events.middleware.EventDebugMiddleware;
import com.tavall.resourcegame.events.middleware.EventDispatchMetricsMiddleware;
import com.tavall.resourcegame.events.middleware.EventDistributedForwardingMiddleware;
import com.tavall.resourcegame.events.middleware.EventMiddlewareCatalog;
import com.tavall.resourcegame.events.middleware.EventPermissionMiddleware;
import com.tavall.resourcegame.events.middleware.EventPermissionPolicy;
import com.tavall.resourcegame.events.middleware.EventRateLimitMiddleware;
import com.tavall.resourcegame.events.middleware.EventRateLimitPolicy;
import com.tavall.resourcegame.events.middleware.EventValidationMiddleware;
import com.tavall.resourcegame.events.middleware.GameEventAuditHandler;
import com.tavall.resourcegame.events.middleware.NoopDistributedEventForwarder;
import com.tavall.resourcegame.events.middleware.RedisGameEventForwarder;
import redis.clients.jedis.JedisPool;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.util.List;

/**
 * Wires the backend game-event pipeline through the repo-local Tavall DI registry.
 */
public final class GameEventDependencyModule implements IDependencyModule {
    @Override
    public void registerDependencies() {
        registerIfMissing(ObjectMapper.class, new ObjectMapper().findAndRegisterModules());
        registerIfMissing(GameEventRedisConfig.class, GameEventRedisConfig.fromEnvironment());
        registerDistributedEventDependencies();
        registerIfMissing(GameEventListenerRegistry.class, new GameEventListenerRegistry());
        registerIfMissing(GameEventAuditHandler.class, new GameEventAuditHandler());
        registerIfMissing(EventDispatchMetricsMiddleware.class, new EventDispatchMetricsMiddleware());
        registerIfMissing(EventPermissionPolicy.class, new AllowAllEventPermissionPolicy());
        registerIfMissing(EventRateLimitPolicy.class, new EventRateLimitPolicy(120, Duration.ofSeconds(10), Clock.systemUTC()));
        registerIfMissing(EventInteractionMappingRegistry.class, new EventInteractionMappingRegistry());
        registerIfMissing(EventMiddlewareCatalog.class, new EventMiddlewareCatalog(List.of(
                new EventValidationMiddleware(),
                new EventCancellationMiddleware(),
                new EventDebugMiddleware(),
                new EventPermissionMiddleware(),
                new EventRateLimitMiddleware(),
                new EventAuditMiddleware(),
                getEventDispatchMetricsMiddleware(),
                new EventDistributedForwardingMiddleware()
        )));
        registerIfMissing(GameEventDispatchHandler.class, new GameEventDispatchHandler());
        registerIfMissing(InteractionBridgeHandler.class, new InteractionBridgeHandler());
    }

    private void registerDistributedEventDependencies() {
        if (getGameEventRedisConfig().distributedEventsEnabled()) {
            registerIfMissing(JedisPool.class, new JedisPool(URI.create(getGameEventRedisConfig().redisUrl())));
            registerIfMissing(DistributedEventForwarder.class, new RedisGameEventForwarder());
            registerIfMissing(RedisGameEventSubscriber.class, new RedisGameEventSubscriber());
            return;
        }
        registerIfMissing(DistributedEventForwarder.class, new NoopDistributedEventForwarder());
    }

    private GameEventRedisConfig getGameEventRedisConfig() {
        return DependencyLoaderAccess.findInstance(GameEventRedisConfig.class);
    }

    private EventDispatchMetricsMiddleware getEventDispatchMetricsMiddleware() {
        return DependencyLoaderAccess.findInstance(EventDispatchMetricsMiddleware.class);
    }

    private <T> void registerIfMissing(Class<T> token, T instance) {
        DependencyLoaderAccess.findOptionalInstance(token)
                .orElseGet(() -> register(token, instance));
    }

    private <T> T register(Class<T> token, T instance) {
        DependencyLoaderAccess.registerInstance(token, instance);
        return instance;
    }
}
