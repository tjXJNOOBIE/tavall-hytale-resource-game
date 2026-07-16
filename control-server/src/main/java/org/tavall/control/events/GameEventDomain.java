package org.tavall.control.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import org.tavall.control.events.dispatch.EventInteractionMappingRegistry;
import org.tavall.control.events.dispatch.GameEventDispatchHandler;
import org.tavall.control.events.dispatch.GameEventListenerRegistry;
import org.tavall.control.events.dispatch.InteractionBridgeHandler;
import org.tavall.control.events.dispatch.RedisGameEventSubscriber;
import org.tavall.control.events.middleware.DistributedEventForwarder;
import org.tavall.control.events.middleware.EventDispatchMetricsMiddleware;
import org.tavall.control.events.middleware.EventMiddlewareCatalog;
import org.tavall.control.events.middleware.EventPermissionPolicy;
import org.tavall.control.events.middleware.EventRateLimitPolicy;
import org.tavall.control.events.middleware.GameEventAuditHandler;
import redis.clients.jedis.JedisPool;

public interface GameEventDomain {
    default ObjectMapper getGameEventObjectMapper() {
        return DependencyLoaderAccess.findInstance(ObjectMapper.class);
    }

    default JedisPool getGameEventRedisPool() {
        return DependencyLoaderAccess.findInstance(JedisPool.class);
    }

    default GameEventRedisConfig getGameEventRedisConfig() {
        return DependencyLoaderAccess.findInstance(GameEventRedisConfig.class);
    }

    default GameEventListenerRegistry getGameEventListenerRegistry() {
        return DependencyLoaderAccess.findInstance(GameEventListenerRegistry.class);
    }

    default GameEventAuditHandler getGameEventAuditHandler() {
        return DependencyLoaderAccess.findInstance(GameEventAuditHandler.class);
    }

    default EventDispatchMetricsMiddleware getEventDispatchMetricsMiddleware() {
        return DependencyLoaderAccess.findInstance(EventDispatchMetricsMiddleware.class);
    }

    default EventPermissionPolicy getEventPermissionPolicy() {
        return DependencyLoaderAccess.findInstance(EventPermissionPolicy.class);
    }

    default DistributedEventForwarder getDistributedEventForwarder() {
        return DependencyLoaderAccess.findInstance(DistributedEventForwarder.class);
    }

    default EventRateLimitPolicy getEventRateLimitPolicy() {
        return DependencyLoaderAccess.findInstance(EventRateLimitPolicy.class);
    }

    default EventMiddlewareCatalog getEventMiddlewareCatalog() {
        return DependencyLoaderAccess.findInstance(EventMiddlewareCatalog.class);
    }

    default EventInteractionMappingRegistry getEventInteractionMappingRegistry() {
        return DependencyLoaderAccess.findInstance(EventInteractionMappingRegistry.class);
    }

    default GameEventDispatchHandler getGameEventDispatchHandler() {
        return DependencyLoaderAccess.findInstance(GameEventDispatchHandler.class);
    }

    default InteractionBridgeHandler getInteractionBridgeHandler() {
        return DependencyLoaderAccess.findInstance(InteractionBridgeHandler.class);
    }

    default RedisGameEventSubscriber getRedisGameEventSubscriber() {
        return DependencyLoaderAccess.findInstance(RedisGameEventSubscriber.class);
    }
}
