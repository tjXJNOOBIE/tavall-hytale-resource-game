package org.tavall.control.liveops;

import org.tavall.dependency.IDependencyInjectableConcrete;
import org.tavall.control.events.dispatch.GameEventDispatchHandler;
import org.tavall.control.liveops.config.FeatureFlagHandler;
import org.tavall.control.liveops.config.GameRuleHandler;
import org.tavall.control.liveops.config.InMemoryLiveConfigChangePublisher;
import org.tavall.control.liveops.config.LiveConfigMutationHandler;
import org.tavall.control.liveops.config.LiveConfigRegistry;
import org.tavall.control.liveops.config.LiveConfigRepository;
import org.tavall.control.liveops.config.SystemToggleHandler;
import org.tavall.control.liveops.gui.GlobalGuiMutationHandler;
import org.tavall.control.liveops.gui.GlobalGuiRegistry;
import org.tavall.control.liveops.gui.GlobalGuiRepository;
import org.tavall.control.liveops.gui.InMemoryGlobalGuiChangePublisher;

public final class LiveOpsRuntime implements LiveOpsDomain, IDependencyInjectableConcrete {
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
