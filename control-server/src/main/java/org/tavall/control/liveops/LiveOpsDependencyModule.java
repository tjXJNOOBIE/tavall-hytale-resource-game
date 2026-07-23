package org.tavall.control.liveops;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.tavall.dependency.DependencyLoaderAccess;
import org.tavall.dependency.IDependencyModule;
import org.tavall.control.liveops.config.FeatureFlagHandler;
import org.tavall.control.liveops.config.GameRuleHandler;
import org.tavall.control.liveops.config.InMemoryLiveConfigChangePublisher;
import org.tavall.control.liveops.config.InMemoryLiveConfigRepository;
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
import org.tavall.control.liveops.gui.InMemoryGlobalGuiRepository;

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
