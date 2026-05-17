package com.tavall.resourcegame.liveops;

import com.tavall.resourcegame.dependency.DependencyLoaderAccess;
import com.tavall.resourcegame.events.core.GameEventType;
import com.tavall.resourcegame.events.dispatch.GameEventDispatchRuntime;
import com.tavall.resourcegame.events.dispatch.GameEventDispatchRuntimeFactory;
import com.tavall.resourcegame.liveops.config.GameSystemToggle;
import com.tavall.resourcegame.liveops.config.LiveConfigChangeRequest;
import com.tavall.resourcegame.liveops.config.LiveConfigDisabledException;
import com.tavall.resourcegame.liveops.config.LiveConfigRolloutStrategy;
import com.tavall.resourcegame.liveops.config.LiveConfigType;
import com.tavall.resourcegame.liveops.gui.GlobalGuiChangeRequest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class LiveOpsRuntimeIntegrationTest {
    @Test
    void liveConfigChangeUpdatesMemoryPublishesAndFiresGameEvent() {
        DependencyLoaderAccess.clear();
        GameEventDispatchRuntime eventRuntime = GameEventDispatchRuntimeFactory.createInMemoryRuntime();
        List<GameEventType> observedEvents = new ArrayList<>();
        eventRuntime.listenerRegistry().registerGlobal(context -> observedEvents.add(context.event().getEventType()));
        LiveOpsRuntime runtime = LiveOpsRuntimeFactory.createInMemoryRuntime();

        runtime.liveConfigMutationHandler().applyChange(new LiveConfigChangeRequest(
                "castle.placement.enabled",
                LiveConfigType.FEATURE_FLAG,
                "true",
                true,
                "local",
                "admin",
                "Castle placement switch",
                LiveConfigRolloutStrategy.global(),
                Map.of()
        ));
        runtime.liveConfigMutationHandler().applyChange(new LiveConfigChangeRequest(
                GameSystemToggle.CITIZEN_AGING.configKey(),
                LiveConfigType.SYSTEM_TOGGLE,
                "false",
                false,
                "local",
                "admin",
                "Disable aging while debugging",
                LiveConfigRolloutStrategy.global(),
                Map.of()
        ));
        runtime.liveConfigMutationHandler().applyChange(new LiveConfigChangeRequest(
                "items.arcane_lance.enabled",
                LiveConfigType.ITEM_TOGGLE,
                "false",
                false,
                "local",
                "admin",
                "Vault test item",
                LiveConfigRolloutStrategy.global(),
                Map.of()
        ));

        assertTrue(runtime.featureFlagHandler().isEnabled("castle.placement.enabled"));
        assertEquals(1L, runtime.liveConfigRegistry().getVersion("castle.placement.enabled"));
        assertEquals(3, runtime.liveConfigPublisher().changes().size());
        assertThrows(LiveConfigDisabledException.class, () -> runtime.systemToggleHandler().requireEnabled(GameSystemToggle.CITIZEN_AGING));
        assertTrue(observedEvents.contains(GameEventType.FEATURE_FLAG_CHANGED));
        assertTrue(observedEvents.contains(GameEventType.SYSTEM_TOGGLE_CHANGED));
        assertTrue(observedEvents.contains(GameEventType.ITEM_VAULTED));
    }

    @Test
    void globalGuiChangeLoadsIntoMemoryAndFiresRefreshEvent() {
        DependencyLoaderAccess.clear();
        GameEventDispatchRuntime eventRuntime = GameEventDispatchRuntimeFactory.createInMemoryRuntime();
        List<GameEventType> observedEvents = new ArrayList<>();
        eventRuntime.listenerRegistry().registerGlobal(context -> observedEvents.add(context.event().getEventType()));
        LiveOpsRuntime runtime = LiveOpsRuntimeFactory.createInMemoryRuntime();

        runtime.globalGuiMutationHandler().applyChange(new GlobalGuiChangeRequest(
                "gui.liveops.main",
                "LiveOps",
                "{\"sections\":[{\"id\":\"toggles\"}],\"buttons\":[{\"id\":\"vault-item\"}]}",
                true,
                "admin",
                Map.of()
        ));

        assertEquals(1L, runtime.globalGuiRegistry().getVersion("gui.liveops.main"));
        assertEquals("toggles", runtime.globalGuiRegistry().layout("gui.liveops.main").get("sections").get(0).get("id").asText());
        assertEquals(1, runtime.globalGuiPublisher().changes().size());
        assertTrue(observedEvents.contains(GameEventType.GLOBAL_GUI_UPDATED));
    }
}
