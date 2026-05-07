package com.tavall.hytale.resourcegame.liveops;

import com.tavall.hytale.resourcegame.events.dispatch.GameEventDispatchHandler;
import com.tavall.hytale.resourcegame.liveops.config.FeatureFlagHandler;
import com.tavall.hytale.resourcegame.liveops.config.GameRuleHandler;
import com.tavall.hytale.resourcegame.liveops.config.InMemoryLiveConfigChangePublisher;
import com.tavall.hytale.resourcegame.liveops.config.LiveConfigMutationHandler;
import com.tavall.hytale.resourcegame.liveops.config.LiveConfigRegistry;
import com.tavall.hytale.resourcegame.liveops.config.LiveConfigRepository;
import com.tavall.hytale.resourcegame.liveops.config.SystemToggleHandler;
import com.tavall.hytale.resourcegame.liveops.gui.GlobalGuiMutationHandler;
import com.tavall.hytale.resourcegame.liveops.gui.GlobalGuiRegistry;
import com.tavall.hytale.resourcegame.liveops.gui.GlobalGuiRepository;
import com.tavall.hytale.resourcegame.liveops.gui.InMemoryGlobalGuiChangePublisher;

public record LiveOpsRuntime(
        LiveConfigRepository liveConfigRepository,
        LiveConfigRegistry liveConfigRegistry,
        LiveConfigMutationHandler liveConfigMutationHandler,
        InMemoryLiveConfigChangePublisher liveConfigPublisher,
        FeatureFlagHandler featureFlagHandler,
        GameRuleHandler gameRuleHandler,
        SystemToggleHandler systemToggleHandler,
        GlobalGuiRepository globalGuiRepository,
        GlobalGuiRegistry globalGuiRegistry,
        GlobalGuiMutationHandler globalGuiMutationHandler,
        InMemoryGlobalGuiChangePublisher globalGuiPublisher,
        GameEventDispatchHandler eventDispatchHandler
) {
}
