package org.tavall.control.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import com.tjxjnoobie.api.dependency.IDependencyModule;
import org.tavall.control.events.dispatch.EventInteractionMappingRegistry;
import org.tavall.control.events.dispatch.GameEventDispatchHandler;
import org.tavall.control.events.dispatch.GameEventListenerRegistry;
import org.tavall.control.events.dispatch.InteractionBridgeHandler;
import org.tavall.control.events.dispatch.RedisGameEventSubscriber;
import org.tavall.control.events.middleware.AllowAllEventPermissionPolicy;
import org.tavall.control.events.middleware.DistributedEventForwarder;
import org.tavall.control.events.middleware.EventAuditMiddleware;
import org.tavall.control.events.middleware.EventCancellationMiddleware;
import org.tavall.control.events.middleware.EventDebugMiddleware;
import org.tavall.control.events.middleware.EventDispatchMetricsMiddleware;
import org.tavall.control.events.middleware.EventDistributedForwardingMiddleware;
import org.tavall.control.events.middleware.EventMiddlewareCatalog;
import org.tavall.control.events.middleware.EventPermissionMiddleware;
import org.tavall.control.events.middleware.EventPermissionPolicy;
import org.tavall.control.events.middleware.EventRateLimitMiddleware;
import org.tavall.control.events.middleware.EventRateLimitPolicy;
import org.tavall.control.events.middleware.EventValidationMiddleware;
import org.tavall.control.events.middleware.GameEventAuditHandler;
import org.tavall.control.events.middleware.NoopDistributedEventForwarder;
import org.tavall.control.events.middleware.RedisGameEventForwarder;
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
