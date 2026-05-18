package org.tavall.control.cli;

import org.tavall.control.IControlServerDomain;
import org.tavall.control.runtime.CommandIssuedFrom;
import org.tavall.control.runtime.ControlCommand;
import org.tavall.control.runtime.ControlCommandDefinition;
import org.tavall.control.runtime.ControlCommandResult;
import org.tavall.control.runtime.ControlCommandValidationException;

import java.io.PrintWriter;
import java.time.Instant;
import java.util.Scanner;

public final class ControlConsoleInputHandler implements IControlServerDomain {
    public String executeOneShotCommand(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        String trimmed = input.trim();
        if (trimmed.equalsIgnoreCase("stop")) {
            return "bye" + System.lineSeparator();
        }
        if (trimmed.equalsIgnoreCase("help")) {
            return helpText();
        }
        if (trimmed.equalsIgnoreCase("commands")) {
            return commandList();
        }
        if (trimmed.equalsIgnoreCase("audit recent")) {
            return recentAuditText();
        }
        if (trimmed.regionMatches(true, 0, "cloud ", 0, "cloud ".length())) {
            return getCloudControlCliHandler().execute(trimmed, getWebControlOperator().operatorId(), Instant.now());
        }
        ControlCommand command = getControlCommandRuntime().parsingHandler().parseConsoleCommand(trimmed, getWebControlOperator(), CommandIssuedFrom.CLI, Instant.now());
        ControlCommandResult result = getControlCommandRuntime().dispatchHandler().dispatchCommand(command);
        return getControlConsoleResultRenderer().renderResult(result);
    }

    public void runInteractiveConsole(Scanner scanner, PrintWriter writer) {
        writer.println("Tavall control console. Type help, commands, audit recent, or exit.");
        writer.flush();
        while (scanner.hasNextLine()) {
            writer.print("control> ");
            writer.flush();
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("stop")) {
                writer.println("bye");
                writer.flush();
                return;
            }
            try {
                writer.print(executeOneShotCommand(input));
            } catch (ControlCommandValidationException exception) {
                writer.println("error=" + exception.getMessage());
            }
            writer.flush();
        }
    }

    public String helpText() {
        return String.join(System.lineSeparator(),
                "help",
                "commands",
                "stop",
                "dry-run troop wound <troopId> <woundType> <severity>",
                "execute troop wound <troopId> <woundType> <severity>",
                "player location <universalPlayerId> <platform> <worldId> <x> <y> <z>",
                "resource give <universalPlayerId> <globalAssetId> <amount>",
                "troop heal food <universalPlayerId> <troopId>",
                "troop heal treatment <universalPlayerId> <troopId> <recipeId> <facilityLevel>",
                "tick healing <count>",
                "tick kingdom",
                "tick clock [kingdomId|all]",
                "clock state <kingdomId>",
                "clock mode <kingdomId> <mode>",
                "clock override <kingdomId> <HH:mm>",
                "clock clear-override <kingdomId>",
                "clock config <kingdomId> key=value ...",
                "schedule active <kingdomId>",
                "schedule create <kingdomId> <ruleType> key=value ...",
                "schedule enable <ruleId>",
                "schedule disable <ruleId>",
                "aging policy <kingdomId> key=value ...",
                "aging tick <kingdomId>",
                "kingdom create --displayName <name> --worldId <worldId> --borderSize <size>",
                "kingdom debug <kingdomId>",
                "kingdom scaling evaluate",
                "kingdom border resolve <worldId> <x> <y> <z>",
                "kingdom border update <kingdomId> <minX> <maxX> <minZ> <maxZ> [worldId]",
                "kingdom border simulate-crossing <playerId> <worldId> <fromX> <fromY> <fromZ> <toX> <toY> <toZ>",
                "coord convert <platform> <worldId> <x> <y> <z>",
                "coord params <platform> key=value ...",
                "instance register <platform> <kingdomId> <platformInstanceId> [instanceName]",
                "instance health <platformInstanceId> <state>",
                "instance switch <playerId> <platform> <fromKingdomId> <toKingdomId>",
                "instance routing debug <kingdomId> <platform>",
                "params list",
                "params get <parameterKey> [scopeId]",
                "params set <parameterKey> <value> [scopeType] [scopeId]",
                "params dry-run <parameterKey> <value> [scopeType] [scopeId]",
                "projection refresh [platform]",
                "platform sync <platform|all>",
                "control start web-panel [port]",
                "control web-panel start [port]",
                "broadcast <message>",
                "audit recent",
                "cloud nodes list",
                "cloud nodes inspect <nodeId>",
                "cloud nodes create-token [ttlSeconds] [region]",
                "cloud workloads list",
                "cloud workloads create <type> <name> [region]",
                "cloud workloads inspect <workloadId>",
                "cloud workloads start <workloadId>",
                "cloud workloads stop <workloadId>",
                "cloud workloads restart <workloadId>",
                "cloud workloads delete <workloadId>",
                "cloud workloads reconcile <workloadId>",
                "cloud workloads reconcile-all",
                "cloud ports list",
                "cloud ports allocate <workloadId> <publicPort> [internalPort] [TCP|UDP]",
                "cloud volumes list",
                "cloud firewall list",
                "cloud proxy routes list",
                "cloud dns records list",
                "cloud backups list",
                "cloud backups run <workloadId> <sourcePath> <destination>",
                "cloud scheduler plan <type> <name> [region]",
                "cloud scheduler candidates <type> <name> [region]",
                "cloud commands list",
                "cloud commands inspect <commandId>",
                "cloud commands retry <commandId>",
                "cloud commands cancel <commandId>",
                "cloud alerts list",
                "cloud alerts ack <alertId>",
                "cloud metrics node <nodeId>",
                "cloud metrics workload <workloadId>",
                "exit",
                "");
    }

    private String commandList() {
        StringBuilder builder = new StringBuilder();
        for (ControlCommandDefinition definition : getControlCommandRuntime().commandRegistry().definitions()) {
            builder.append(definition.commandType())
                    .append(" - ")
                    .append(definition.displayName())
                    .append(System.lineSeparator());
        }
        return builder.toString();
    }

    private String recentAuditText() {
        StringBuilder builder = new StringBuilder();
        getControlCommandRuntime().auditLogRepository().findRecentAuditLogs(10).forEach(auditLog -> builder
                .append(auditLog.commandType())
                .append(" ")
                .append(auditLog.resultState())
                .append(" ")
                .append(auditLog.message())
                .append(System.lineSeparator()));
        return builder.toString();
    }
}
