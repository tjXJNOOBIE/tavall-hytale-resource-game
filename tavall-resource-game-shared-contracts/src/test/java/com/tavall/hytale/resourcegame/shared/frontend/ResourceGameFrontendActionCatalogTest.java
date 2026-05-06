package com.tavall.hytale.resourcegame.shared.frontend;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public final class ResourceGameFrontendActionCatalogTest {
    @Test
    void minecraftCastleActionUsesMinecraftInteractionType() {
        ResourceGameFrontendActionCatalog catalog = new ResourceGameFrontendActionCatalog();

        List<ResourceGameFrontendActionDescriptor> actions = catalog.actionsFor(ResourceGameFrontendPlatform.MINECRAFT, ResourceGameFrontendObjectKind.CASTLE);

        assertEquals("kingdom.castle.info", actions.getFirst().actionId());
        assertEquals("MINECRAFT_COMMAND", actions.getFirst().interactionType());
    }

    @Test
    void allLivePlatformsExposePetitionActions() {
        ResourceGameFrontendActionCatalog catalog = new ResourceGameFrontendActionCatalog();

        assertFalse(catalog.actionsFor(ResourceGameFrontendPlatform.HYTALE, ResourceGameFrontendObjectKind.PETITION).isEmpty());
        assertFalse(catalog.actionsFor(ResourceGameFrontendPlatform.MINECRAFT, ResourceGameFrontendObjectKind.PETITION).isEmpty());
        assertFalse(catalog.actionsFor(ResourceGameFrontendPlatform.ROBLOX, ResourceGameFrontendObjectKind.PETITION).isEmpty());
        assertFalse(catalog.actionsFor(ResourceGameFrontendPlatform.DISCORD, ResourceGameFrontendObjectKind.PETITION).isEmpty());
    }

    @Test
    void allLivePlatformsExposeClockAndScheduleActions() {
        ResourceGameFrontendActionCatalog catalog = new ResourceGameFrontendActionCatalog();

        assertEquals("hytale.clock.view", catalog.actionsFor(ResourceGameFrontendPlatform.HYTALE, ResourceGameFrontendObjectKind.KINGDOM_CLOCK).getFirst().actionId());
        assertEquals("kingdom.clock.view", catalog.actionsFor(ResourceGameFrontendPlatform.MINECRAFT, ResourceGameFrontendObjectKind.KINGDOM_CLOCK).getFirst().actionId());
        assertEquals("roblox.clock.gui", catalog.actionsFor(ResourceGameFrontendPlatform.ROBLOX, ResourceGameFrontendObjectKind.KINGDOM_CLOCK).getFirst().actionId());
        assertEquals("discord.clock.view", catalog.actionsFor(ResourceGameFrontendPlatform.DISCORD, ResourceGameFrontendObjectKind.KINGDOM_CLOCK).getFirst().actionId());

        assertEquals("hytale.schedule.view", catalog.actionsFor(ResourceGameFrontendPlatform.HYTALE, ResourceGameFrontendObjectKind.KINGDOM_SCHEDULE).getFirst().actionId());
        assertEquals("kingdom.schedule.view", catalog.actionsFor(ResourceGameFrontendPlatform.MINECRAFT, ResourceGameFrontendObjectKind.KINGDOM_SCHEDULE).getFirst().actionId());
        assertEquals("roblox.schedule.gui", catalog.actionsFor(ResourceGameFrontendPlatform.ROBLOX, ResourceGameFrontendObjectKind.KINGDOM_SCHEDULE).getFirst().actionId());
        assertEquals("discord.schedule.view", catalog.actionsFor(ResourceGameFrontendPlatform.DISCORD, ResourceGameFrontendObjectKind.KINGDOM_SCHEDULE).getFirst().actionId());
    }
}
