package com.tavall.resourcegame.frontend.minecraft.server;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class KingdomCommandRouterTest {
    @Test
    void rootSuggestionsIncludeTheSplitKingdomSurfaceGroups() {
        KingdomCommandRouter router = new KingdomCommandRouter();

        List<String> suggestions = router.onTabComplete(null, null, null, new String[0]);

        assertTrue(suggestions.contains("help"));
        assertTrue(suggestions.contains("ui"));
        assertTrue(suggestions.contains("data"));
        assertTrue(suggestions.contains("account"));
        assertTrue(suggestions.contains("castle"));
        assertTrue(suggestions.contains("companion"));
        assertTrue(suggestions.contains("npc"));
        assertTrue(suggestions.contains("building"));
    }
}
