package com.tavall.resourcegame.liveops;

import com.tavall.resourcegame.dependency.IDependencyInjectableConcrete;
import com.tavall.resourcegame.events.dispatch.GameEventDispatchHandler;
import com.tavall.resourcegame.liveops.config.FeatureFlagHandler;
import com.tavall.resourcegame.liveops.config.GameRuleHandler;
import com.tavall.resourcegame.liveops.config.InMemoryLiveConfigChangePublisher;
import com.tavall.resourcegame.liveops.config.LiveConfigMutationHandler;
import com.tavall.resourcegame.liveops.config.LiveConfigRegistry;
import com.tavall.resourcegame.liveops.config.LiveConfigRepository;
import com.tavall.resourcegame.liveops.config.SystemToggleHandler;
import com.tavall.resourcegame.liveops.gui.GlobalGuiMutationHandler;
import com.tavall.resourcegame.liveops.gui.GlobalGuiRegistry;
import com.tavall.resourcegame.liveops.gui.GlobalGuiRepository;
import com.tavall.resourcegame.liveops.gui.InMemoryGlobalGuiChangePublisher;

public final class LiveOpsRuntime implements ILiveOpsDomain, IDependencyInjectableConcrete {
    public LiveConfigRepository liveConfigRepository() {
        return getLiveConfigRepository();
    }

    public LiveConfigRegistry liveConfigRegistry() {
        return getLiveConfigRegistry();
    }

    public LiveConfigMutationHandler liveConfigMutationHandler() {
        return getLiveConfigMutationHandler();
    }

    public InMemoryLiveConfigChangePublisher liveConfigPublisher() {
        return getInMemoryLiveConfigChangePublisher();
    }

    public FeatureFlagHandler featureFlagHandler() {
        return getFeatureFlagHandler();
    }

    public GameRuleHandler gameRuleHandler() {
        return getGameRuleHandler();
    }

    public SystemToggleHandler systemToggleHandler() {
        return getSystemToggleHandler();
    }

    public GlobalGuiRepository globalGuiRepository() {
        return getGlobalGuiRepository();
    }

    public GlobalGuiRegistry globalGuiRegistry() {
        return getGlobalGuiRegistry();
    }

    public GlobalGuiMutationHandler globalGuiMutationHandler() {
        return getGlobalGuiMutationHandler();
    }

    public InMemoryGlobalGuiChangePublisher globalGuiPublisher() {
        return getInMemoryGlobalGuiChangePublisher();
    }

    public GameEventDispatchHandler eventDispatchHandler() {
        return getOptionalGameEventDispatchHandler().orElse(null);
    }
}
