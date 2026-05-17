package com.tavall.resourcegame.liveops;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import com.tavall.resourcegame.events.dispatch.GameEventDispatchHandler;
import com.tavall.resourcegame.liveops.config.FeatureFlagHandler;
import com.tavall.resourcegame.liveops.config.GameRuleHandler;
import com.tavall.resourcegame.liveops.config.InMemoryLiveConfigChangePublisher;
import com.tavall.resourcegame.liveops.config.LiveConfigChangePublisher;
import com.tavall.resourcegame.liveops.config.LiveConfigMutationHandler;
import com.tavall.resourcegame.liveops.config.LiveConfigRegistry;
import com.tavall.resourcegame.liveops.config.LiveConfigRepository;
import com.tavall.resourcegame.liveops.config.SystemToggleHandler;
import com.tavall.resourcegame.liveops.gui.GlobalGuiChangePublisher;
import com.tavall.resourcegame.liveops.gui.GlobalGuiMutationHandler;
import com.tavall.resourcegame.liveops.gui.GlobalGuiRegistry;
import com.tavall.resourcegame.liveops.gui.GlobalGuiRepository;
import com.tavall.resourcegame.liveops.gui.InMemoryGlobalGuiChangePublisher;
import com.tavall.resourcegame.persistence.PostgresConnectionProvider;
import redis.clients.jedis.JedisPool;

import java.util.Optional;

public interface ILiveOpsDomainGenerated {
    default ObjectMapper getLiveOpsObjectMapper() {
        return DependencyLoaderAccess.findInstance(ObjectMapper.class);
    }

    default JedisPool getLiveOpsRedisPool() {
        return DependencyLoaderAccess.findInstance(JedisPool.class);
    }

    default PostgresConnectionProvider getLiveOpsPostgresConnectionProvider() {
        return DependencyLoaderAccess.findInstance(PostgresConnectionProvider.class);
    }

    default LiveOpsRedisConfig getLiveOpsRedisConfig() {
        return DependencyLoaderAccess.findInstance(LiveOpsRedisConfig.class);
    }

    default LiveConfigRepository getLiveConfigRepository() {
        return DependencyLoaderAccess.findInstance(LiveConfigRepository.class);
    }

    default LiveConfigRegistry getLiveConfigRegistry() {
        return DependencyLoaderAccess.findInstance(LiveConfigRegistry.class);
    }

    default LiveConfigChangePublisher getLiveConfigChangePublisher() {
        return DependencyLoaderAccess.findInstance(LiveConfigChangePublisher.class);
    }

    default InMemoryLiveConfigChangePublisher getInMemoryLiveConfigChangePublisher() {
        return DependencyLoaderAccess.findInstance(InMemoryLiveConfigChangePublisher.class);
    }

    default LiveConfigMutationHandler getLiveConfigMutationHandler() {
        return DependencyLoaderAccess.findInstance(LiveConfigMutationHandler.class);
    }

    default FeatureFlagHandler getFeatureFlagHandler() {
        return DependencyLoaderAccess.findInstance(FeatureFlagHandler.class);
    }

    default GameRuleHandler getGameRuleHandler() {
        return DependencyLoaderAccess.findInstance(GameRuleHandler.class);
    }

    default SystemToggleHandler getSystemToggleHandler() {
        return DependencyLoaderAccess.findInstance(SystemToggleHandler.class);
    }

    default GlobalGuiRepository getGlobalGuiRepository() {
        return DependencyLoaderAccess.findInstance(GlobalGuiRepository.class);
    }

    default GlobalGuiRegistry getGlobalGuiRegistry() {
        return DependencyLoaderAccess.findInstance(GlobalGuiRegistry.class);
    }

    default GlobalGuiChangePublisher getGlobalGuiChangePublisher() {
        return DependencyLoaderAccess.findInstance(GlobalGuiChangePublisher.class);
    }

    default InMemoryGlobalGuiChangePublisher getInMemoryGlobalGuiChangePublisher() {
        return DependencyLoaderAccess.findInstance(InMemoryGlobalGuiChangePublisher.class);
    }

    default GlobalGuiMutationHandler getGlobalGuiMutationHandler() {
        return DependencyLoaderAccess.findInstance(GlobalGuiMutationHandler.class);
    }

    default Optional<GameEventDispatchHandler> getOptionalGameEventDispatchHandler() {
        return DependencyLoaderAccess.findOptionalInstance(GameEventDispatchHandler.class);
    }
}
