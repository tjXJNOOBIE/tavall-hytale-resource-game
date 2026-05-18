package org.tavall.minecraft.server;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class KingdomCompanionCommandTest {
    @Test
    void rootSuggestionsIncludeTheSplitCompanionSubcommands() {
        KingdomCompanionCommand command = new KingdomCompanionCommand();

        List<String> suggestions = command.onTabComplete(null, null, null, new String[0]);

        assertTrue(suggestions.contains("skill"));
        assertTrue(suggestions.contains("summon"));
        assertTrue(suggestions.contains("wall"));
    }

    @Test
    void nestedSkillSuggestionsExposeTheInnerActionTokens() {
        KingdomCompanionCommand command = new KingdomCompanionCommand();

        List<String> suggestions = command.onTabComplete(null, null, null, new String[]{"skill", "u"});

        assertTrue(suggestions.contains("unlock"));
        assertTrue(suggestions.contains("upgrade"));
    }
}
