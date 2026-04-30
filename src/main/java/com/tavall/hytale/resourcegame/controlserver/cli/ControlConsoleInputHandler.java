package com.tavall.hytale.resourcegame.controlserver.cli;

import com.tavall.hytale.resourcegame.middleware.control.CommandIssuedFrom;
import com.tavall.hytale.resourcegame.middleware.control.ControlCommand;
import com.tavall.hytale.resourcegame.middleware.control.ControlCommandDefinition;
import com.tavall.hytale.resourcegame.middleware.control.ControlCommandResult;
import com.tavall.hytale.resourcegame.middleware.control.ControlCommandRuntime;
import com.tavall.hytale.resourcegame.middleware.control.ControlCommandValidationException;
import com.tavall.hytale.resourcegame.middleware.control.ControlOperator;

import java.io.PrintWriter;
import java.time.Instant;
import java.util.Scanner;

public final class ControlConsoleInputHandler {
    private final ControlCommandRuntime runtime;
    private final ControlConsoleResultRenderer resultRenderer;
    private final ControlOperator operator;

    public ControlConsoleInputHandler(ControlCommandRuntime runtime, ControlConsoleResultRenderer resultRenderer, ControlOperator operator) {
        this.runtime = runtime;
        this.resultRenderer = resultRenderer;
        this.operator = operator;
    }

    public String executeOneShotCommand(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        String trimmed = input.trim();
        if (trimmed.equalsIgnoreCase("help")) {
            return helpText();
        }
        if (trimmed.equalsIgnoreCase("commands")) {
            return commandList();
        }
        if (trimmed.equalsIgnoreCase("audit recent")) {
            return recentAuditText();
        }
        ControlCommand command = runtime.parsingHandler().parseConsoleCommand(trimmed, operator, CommandIssuedFrom.CLI, Instant.now());
        ControlCommandResult result = runtime.dispatchHandler().dispatchCommand(command);
        return resultRenderer.renderResult(result);
    }

    public void runInteractiveConsole(Scanner scanner, PrintWriter writer) {
        writer.println("Tavall control console. Type help, commands, audit recent, or exit.");
        writer.flush();
        while (scanner.hasNextLine()) {
            writer.print("control> ");
            writer.flush();
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("quit")) {
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
                "dry-run troop wound <troopId> <woundType> <severity>",
                "execute troop wound <troopId> <woundType> <severity>",
                "resource give <universalPlayerId> <globalAssetId> <amount>",
                "troop heal food <universalPlayerId> <troopId>",
                "troop heal treatment <universalPlayerId> <troopId> <recipeId> <facilityLevel>",
                "tick healing <count>",
                "projection refresh [platform]",
                "platform sync <platform|all>",
                "broadcast <message>",
                "audit recent",
                "exit",
                "");
    }

    private String commandList() {
        StringBuilder builder = new StringBuilder();
        for (ControlCommandDefinition definition : runtime.commandRegistry().definitions()) {
            builder.append(definition.commandType())
                    .append(" - ")
                    .append(definition.displayName())
                    .append(System.lineSeparator());
        }
        return builder.toString();
    }

    private String recentAuditText() {
        StringBuilder builder = new StringBuilder();
        runtime.auditLogRepository().findRecentAuditLogs(10).forEach(auditLog -> builder
                .append(auditLog.commandType())
                .append(" ")
                .append(auditLog.resultState())
                .append(" ")
                .append(auditLog.message())
                .append(System.lineSeparator()));
        return builder.toString();
    }
}
