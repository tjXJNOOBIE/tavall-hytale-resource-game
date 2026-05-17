package com.tavall.resourcegame.frontend.minecraft.server;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class KingdomUiCommandTest {
    @Test
    void rootSuggestionsIncludeTheNewGuiHubTokens() {
        KingdomUiCommand command = new KingdomUiCommand();

        List<String> suggestions = command.onTabComplete(null, null, null, new String[0]);

        assertTrue(suggestions.contains("admin"));
        assertTrue(suggestions.contains("castle"));
        assertTrue(suggestions.contains("debug"));
        assertTrue(suggestions.contains("account"));
        assertTrue(suggestions.contains("npc"));
        assertTrue(suggestions.contains("building"));
    }
}
