package com.tavall.hytale.resourcegame.middleware.control;

import com.tavall.hytale.resourcegame.middleware.common.GamePlatform;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ControlCommandParsingIntegrationTest {
    @Test
    void consoleParserCreatesTypedDryRunTroopWoundCommand() {
        ControlCommandParsingHandler parser = new ControlCommandParsingHandler();
        ControlOperator operator = ControlOperator.localOwner(Instant.parse("2026-04-30T16:00:00Z"));
        UUID troopId = UUID.randomUUID();

        ControlCommand command = parser.parseConsoleCommand(
                "dry-run troop wound " + troopId + " GENERAL_WOUND MODERATE",
                operator,
                CommandIssuedFrom.CLI,
                Instant.parse("2026-04-30T16:01:00Z")
        );

        assertEquals(ControlCommandType.ASSIGN_TROOP_WOUND, command.commandType());
        assertEquals(CommandTargetScope.TROOP, command.targetScope());
        assertEquals("GENERAL_WOUND", command.argument("woundType"));
        assertEquals("MODERATE", command.argument("severity"));
        assertTrue(command.dryRun());
    }

    @Test
    void consoleParserPreservesQuotedBroadcastMessageAndTargetPlatform() {
        ControlCommandParsingHandler parser = new ControlCommandParsingHandler();
        ControlOperator operator = ControlOperator.localOwner(Instant.parse("2026-04-30T16:00:00Z"));

        ControlCommand refresh = parser.parseConsoleCommand("projection refresh minecraft", operator, CommandIssuedFrom.CLI, Instant.now());
        ControlCommand broadcast = parser.parseConsoleCommand("broadcast \"server maintenance soon\"", operator, CommandIssuedFrom.CLI, Instant.now());

        assertEquals(ControlCommandType.REFRESH_FRONTEND_PROJECTIONS, refresh.commandType());
        assertTrue(refresh.targetPlatforms().contains(GamePlatform.MINECRAFT));
        assertEquals("server maintenance soon", broadcast.argument("message"));
    }

    @Test
    void parserRejectsUnknownAndMalformedCommands() {
        ControlCommandParsingHandler parser = new ControlCommandParsingHandler();
        ControlOperator operator = ControlOperator.localOwner(Instant.now());

        assertThrows(ControlCommandValidationException.class, () -> parser.parseConsoleCommand("unknown thing", operator, CommandIssuedFrom.CLI, Instant.now()));
        assertThrows(ControlCommandValidationException.class, () -> parser.parseConsoleCommand("troop wound only-two", operator, CommandIssuedFrom.CLI, Instant.now()));
    }
}
