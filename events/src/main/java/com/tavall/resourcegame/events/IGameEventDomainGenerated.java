package com.tavall.resourcegame.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tavall.resourcegame.dependency.DependencyLoaderAccess;
import com.tavall.resourcegame.events.dispatch.EventInteractionMappingRegistry;
import com.tavall.resourcegame.events.dispatch.GameEventDispatchHandler;
import com.tavall.resourcegame.events.dispatch.GameEventListenerRegistry;
import com.tavall.resourcegame.events.dispatch.InteractionBridgeHandler;
import com.tavall.resourcegame.events.dispatch.RedisGameEventSubscriber;
import com.tavall.resourcegame.events.middleware.DistributedEventForwarder;
import com.tavall.resourcegame.events.middleware.EventDispatchMetricsMiddleware;
import com.tavall.resourcegame.events.middleware.EventMiddlewareCatalog;
import com.tavall.resourcegame.events.middleware.EventPermissionPolicy;
import com.tavall.resourcegame.events.middleware.EventRateLimitPolicy;
import com.tavall.resourcegame.events.middleware.GameEventAuditHandler;
import redis.clients.jedis.JedisPool;

public interface IGameEventDomainGenerated {
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
