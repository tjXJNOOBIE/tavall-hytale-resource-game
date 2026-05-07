package com.tavall.hytale.resourcegame.frontend.discord;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class DiscordCommandTokenParserTest {
    @Test
    void parsesWhitespaceSeparatedControlCommand() {
        DiscordCommandTokenParser parser = new DiscordCommandTokenParser();

        assertEquals(
                List.of("troop", "wound", "troop-1", "GENERAL_WOUND", "MODERATE"),
                parser.parseCommandTokens("troop wound troop-1 GENERAL_WOUND MODERATE")
        );
    }

    @Test
    void preservesQuotedMessageAsSingleToken() {
        DiscordCommandTokenParser parser = new DiscordCommandTokenParser();

        assertEquals(
                List.of("broadcast", "The guild route is active"),
                parser.parseCommandTokens("broadcast \"The guild route is active\"")
        );
    }
}
