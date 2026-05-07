package com.tavall.hytale.resourcegame.controlserver.cli;

import com.tavall.hytale.resourcegame.middleware.common.CanonicalLocation;
import com.tavall.hytale.resourcegame.middleware.control.ControlCommandRuntime;
import com.tavall.hytale.resourcegame.middleware.control.ControlCommandRuntimeFactory;
import com.tavall.hytale.resourcegame.middleware.control.ControlOperator;
import com.tavall.hytale.resourcegame.middleware.healing.WoundSeverity;
import com.tavall.hytale.resourcegame.middleware.healing.WoundType;
import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;
import com.tavall.hytale.resourcegame.middleware.troop.Troop;
import com.tavall.hytale.resourcegame.middleware.troop.TroopRegistrationHandler;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ControlConsoleInputHandlerIntegrationTest {
    @Test
    void helpCommandsDryRunAndExecuteUseSharedDispatchPipeline() {
        ControlCommandRuntime runtime = ControlCommandRuntimeFactory.createInMemoryRuntime();
        ControlConsoleInputHandler console = new ControlConsoleInputHandler(runtime, new ControlConsoleResultRenderer(), ControlOperator.localOwner(Instant.now()));
        UniversalPlayerId playerId = UniversalPlayerId.random();
        Troop troop = new TroopRegistrationHandler(runtime.troopRepository())
                .registerTroop(Optional.of(playerId), Optional.empty(), "infantry", 1, new CanonicalLocation("world", 3.0d, 64.0d, 7.0d));

        String help = console.executeOneShotCommand("help");
        String commands = console.executeOneShotCommand("commands");
        String dryRun = console.executeOneShotCommand("dry-run troop wound " + troop.troopId().value() + " " + WoundType.GENERAL_WOUND + " " + WoundSeverity.MINOR);
        String execute = console.executeOneShotCommand("execute troop wound " + troop.troopId().value() + " " + WoundType.GENERAL_WOUND + " " + WoundSeverity.MINOR);
        String audit = console.executeOneShotCommand("audit recent");

        assertTrue(help.contains("troop wound"));
        assertTrue(commands.contains("ASSIGN_TROOP_WOUND"));
        assertTrue(dryRun.contains("state=DRY_RUN_COMPLETED"));
        assertTrue(execute.contains("platform.MINECRAFT=true"));
        assertTrue(runtime.troopHealingRepository().findActiveWoundsForTroop(troop.troopId()).size() == 1);
        assertTrue(audit.contains("ASSIGN_TROOP_WOUND"));
    }
}
