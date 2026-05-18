package org.tavall.control.liveops;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import org.tavall.control.events.dispatch.GameEventDispatchHandler;
import org.tavall.control.liveops.config.FeatureFlagHandler;
import org.tavall.control.liveops.config.GameRuleHandler;
import org.tavall.control.liveops.config.InMemoryLiveConfigChangePublisher;
import org.tavall.control.liveops.config.LiveConfigChangePublisher;
import org.tavall.control.liveops.config.LiveConfigMutationHandler;
import org.tavall.control.liveops.config.LiveConfigRegistry;
import org.tavall.control.liveops.config.LiveConfigRepository;
import org.tavall.control.liveops.config.SystemToggleHandler;
import org.tavall.control.liveops.gui.GlobalGuiChangePublisher;
import org.tavall.control.liveops.gui.GlobalGuiMutationHandler;
import org.tavall.control.liveops.gui.GlobalGuiRegistry;
import org.tavall.control.liveops.gui.GlobalGuiRepository;
import org.tavall.control.liveops.gui.InMemoryGlobalGuiChangePublisher;
import org.tavall.control.persistence.PostgresConnectionProvider;
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
