package org.tavall.control.cli;

import org.tavall.control.ControlServerDependencyModule;
import org.tavall.control.IControlServerDomain;
import org.tavall.control.common.CanonicalLocation;
import org.tavall.control.runtime.ControlCommandRuntime;
import org.tavall.control.healing.WoundSeverity;
import org.tavall.control.healing.WoundType;
import org.tavall.control.identity.UniversalPlayerId;
import org.tavall.control.troop.Troop;
import org.tavall.control.troop.TroopRegistrationHandler;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ControlConsoleInputHandlerIntegrationTest implements IControlServerDomain {
    @Test
    void helpCommandsDryRunAndExecuteUseSharedDispatchPipeline() {
        new ControlServerDependencyModule().registerDependencies();
        ControlCommandRuntime runtime = getControlCommandRuntime();
        ControlConsoleInputHandler console = new ControlConsoleInputHandler();
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

    @Test
    void cloudCommandsUseCloudCliHandler() {
        new ControlServerDependencyModule().registerDependencies();
        ControlConsoleInputHandler console = new ControlConsoleInputHandler();

        String output = console.executeOneShotCommand("cloud nodes list");

        assertTrue(output.contains("nodes=0"));
    }
}
