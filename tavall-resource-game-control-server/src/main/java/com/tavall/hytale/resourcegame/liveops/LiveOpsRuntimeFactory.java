package com.tavall.hytale.resourcegame.liveops;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tavall.hytale.resourcegame.events.dispatch.GameEventDispatchHandler;
import com.tavall.hytale.resourcegame.liveops.config.FeatureFlagHandler;
import com.tavall.hytale.resourcegame.liveops.config.GameRuleHandler;
import com.tavall.hytale.resourcegame.liveops.config.InMemoryLiveConfigChangePublisher;
import com.tavall.hytale.resourcegame.liveops.config.InMemoryLiveConfigRepository;
import com.tavall.hytale.resourcegame.liveops.config.LiveConfigMutationHandler;
import com.tavall.hytale.resourcegame.liveops.config.LiveConfigRegistry;
import com.tavall.hytale.resourcegame.liveops.config.SystemToggleHandler;
import com.tavall.hytale.resourcegame.liveops.gui.GlobalGuiMutationHandler;
import com.tavall.hytale.resourcegame.liveops.gui.GlobalGuiRegistry;
import com.tavall.hytale.resourcegame.liveops.gui.InMemoryGlobalGuiChangePublisher;
import com.tavall.hytale.resourcegame.liveops.gui.InMemoryGlobalGuiRepository;

public final class LiveOpsRuntimeFactory {
    private LiveOpsRuntimeFactory() {
    }

    public static LiveOpsRuntime createInMemoryRuntime(GameEventDispatchHandler eventDispatchHandler) {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        InMemoryLiveConfigRepository liveConfigRepository = new InMemoryLiveConfigRepository();
        LiveConfigRegistry liveConfigRegistry = new LiveConfigRegistry(objectMapper);
        InMemoryLiveConfigChangePublisher liveConfigPublisher = new InMemoryLiveConfigChangePublisher();
        LiveConfigMutationHandler liveConfigMutationHandler = new LiveConfigMutationHandler(
                liveConfigRepository,
                liveConfigRegistry,
                liveConfigPublisher,
                objectMapper,
                eventDispatchHandler
        );
        InMemoryGlobalGuiRepository globalGuiRepository = new InMemoryGlobalGuiRepository();
        GlobalGuiRegistry globalGuiRegistry = new GlobalGuiRegistry(objectMapper);
        InMemoryGlobalGuiChangePublisher globalGuiPublisher = new InMemoryGlobalGuiChangePublisher();
        GlobalGuiMutationHandler globalGuiMutationHandler = new GlobalGuiMutationHandler(
                globalGuiRepository,
                globalGuiRegistry,
                globalGuiPublisher,
                objectMapper,
                eventDispatchHandler
        );
        return new LiveOpsRuntime(
                liveConfigRepository,
                liveConfigRegistry,
                liveConfigMutationHandler,
                liveConfigPublisher,
                new FeatureFlagHandler(liveConfigRegistry),
                new GameRuleHandler(liveConfigRegistry),
                new SystemToggleHandler(liveConfigRegistry),
                globalGuiRepository,
                globalGuiRegistry,
                globalGuiMutationHandler,
                globalGuiPublisher,
                eventDispatchHandler
        );
    }
}
