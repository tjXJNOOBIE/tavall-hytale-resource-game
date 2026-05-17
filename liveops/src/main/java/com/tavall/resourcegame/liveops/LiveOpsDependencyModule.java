package com.tavall.resourcegame.liveops;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tavall.resourcegame.dependency.DependencyLoaderAccess;
import com.tavall.resourcegame.dependency.IDependencyModule;
import com.tavall.resourcegame.liveops.config.FeatureFlagHandler;
import com.tavall.resourcegame.liveops.config.GameRuleHandler;
import com.tavall.resourcegame.liveops.config.InMemoryLiveConfigChangePublisher;
import com.tavall.resourcegame.liveops.config.InMemoryLiveConfigRepository;
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
import com.tavall.resourcegame.liveops.gui.InMemoryGlobalGuiRepository;

/**
 * Keeps LiveOps mutation handlers decoupled from their storage, cache, and event-dispatch adapters.
 */
public final class LiveOpsDependencyModule implements IDependencyModule {
    @Override
    public void registerDependencies() {
        registerIfMissing(ObjectMapper.class, new ObjectMapper().findAndRegisterModules());
        registerIfMissing(LiveOpsRedisConfig.class, LiveOpsRedisConfig.defaults());
        registerIfMissing(LiveConfigRepository.class, new InMemoryLiveConfigRepository());
        registerIfMissing(LiveConfigRegistry.class, new LiveConfigRegistry());
        registerIfMissing(InMemoryLiveConfigChangePublisher.class, new InMemoryLiveConfigChangePublisher());
        registerIfMissing(LiveConfigChangePublisher.class, getInMemoryLiveConfigChangePublisher());
        registerIfMissing(LiveConfigMutationHandler.class, new LiveConfigMutationHandler());
        registerIfMissing(FeatureFlagHandler.class, new FeatureFlagHandler());
        registerIfMissing(GameRuleHandler.class, new GameRuleHandler());
        registerIfMissing(SystemToggleHandler.class, new SystemToggleHandler());
        registerIfMissing(GlobalGuiRepository.class, new InMemoryGlobalGuiRepository());
        registerIfMissing(GlobalGuiRegistry.class, new GlobalGuiRegistry());
        registerIfMissing(InMemoryGlobalGuiChangePublisher.class, new InMemoryGlobalGuiChangePublisher());
        registerIfMissing(GlobalGuiChangePublisher.class, getInMemoryGlobalGuiChangePublisher());
        registerIfMissing(GlobalGuiMutationHandler.class, new GlobalGuiMutationHandler());
    }

    private InMemoryLiveConfigChangePublisher getInMemoryLiveConfigChangePublisher() {
        return DependencyLoaderAccess.findInstance(InMemoryLiveConfigChangePublisher.class);
    }

    private InMemoryGlobalGuiChangePublisher getInMemoryGlobalGuiChangePublisher() {
        return DependencyLoaderAccess.findInstance(InMemoryGlobalGuiChangePublisher.class);
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
