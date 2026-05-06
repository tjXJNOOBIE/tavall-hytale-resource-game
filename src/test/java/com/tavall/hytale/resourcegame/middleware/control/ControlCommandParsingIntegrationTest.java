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
    void consoleParserSupportsControlStartWebPanelPermutations() {
        ControlCommandParsingHandler parser = new ControlCommandParsingHandler();
        ControlOperator operator = ControlOperator.localOwner(Instant.parse("2026-04-30T16:00:00Z"));

        ControlCommand first = parser.parseConsoleCommand("control start web-panel 18090", operator, CommandIssuedFrom.CLI, Instant.now());
        ControlCommand second = parser.parseConsoleCommand("control web start --port 18091", operator, CommandIssuedFrom.CLI, Instant.now());
        ControlCommand third = parser.parseConsoleCommand("web-panel start -p 18092", operator, CommandIssuedFrom.CLI, Instant.now());

        assertEquals(ControlCommandType.START_CONTROL_SURFACE, first.commandType());
        assertEquals("web-panel", first.argument("surface"));
        assertEquals("18090", first.argument("port"));
        assertEquals("18091", second.argument("port"));
        assertEquals("18092", third.argument("port"));
    }

    @Test
    void consoleParserCreatesKingdomClockScheduleAndAgingCommands() {
        ControlCommandParsingHandler parser = new ControlCommandParsingHandler();
        ControlOperator operator = ControlOperator.localOwner(Instant.parse("2026-05-05T16:00:00Z"));

        ControlCommand clockOverride = parser.parseConsoleCommand("clock override kingdom-1 22:00", operator, CommandIssuedFrom.CLI, Instant.now());
        ControlCommand scheduleCreate = parser.parseConsoleCommand("schedule create kingdom-1 SHOP_OPEN startHour=8 endHour=20", operator, CommandIssuedFrom.CLI, Instant.now());
        ControlCommand agingTick = parser.parseConsoleCommand("aging tick kingdom-1", operator, CommandIssuedFrom.CLI, Instant.now());

        assertEquals(ControlCommandType.SET_KINGDOM_TIME_OVERRIDE, clockOverride.commandType());
        assertEquals("22:00", clockOverride.argument("time"));
        assertEquals(ControlCommandType.CREATE_KINGDOM_SCHEDULE_RULE, scheduleCreate.commandType());
        assertEquals("SHOP_OPEN", scheduleCreate.argument("ruleType"));
        assertEquals(ControlCommandType.RUN_AGING_TICK, agingTick.commandType());
    }

    @Test
    void consoleParserCreatesInstanceSwitchCompletionCommands() {
        ControlCommandParsingHandler parser = new ControlCommandParsingHandler();
        ControlOperator operator = ControlOperator.localOwner(Instant.parse("2026-05-06T16:00:00Z"));

        ControlCommand confirm = parser.parseConsoleCommand("instance confirm switch-1", operator, CommandIssuedFrom.CLI, Instant.now());
        ControlCommand fail = parser.parseConsoleCommand("instance fail switch-2 velocity-target-missing", operator, CommandIssuedFrom.CLI, Instant.now());

        assertEquals(ControlCommandType.CONFIRM_INSTANCE_SWITCH, confirm.commandType());
        assertEquals("switch-1", confirm.argument("switchRequestId"));
        assertEquals(ControlCommandType.FAIL_INSTANCE_SWITCH, fail.commandType());
        assertEquals("switch-2", fail.argument("switchRequestId"));
        assertEquals("velocity-target-missing", fail.argument("reason"));
    }

    @Test
    void parserRejectsUnknownAndMalformedCommands() {
        ControlCommandParsingHandler parser = new ControlCommandParsingHandler();
        ControlOperator operator = ControlOperator.localOwner(Instant.now());

        assertThrows(ControlCommandValidationException.class, () -> parser.parseConsoleCommand("unknown thing", operator, CommandIssuedFrom.CLI, Instant.now()));
        assertThrows(ControlCommandValidationException.class, () -> parser.parseConsoleCommand("troop wound only-two", operator, CommandIssuedFrom.CLI, Instant.now()));
    }
}
