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
}
