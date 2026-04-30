package com.tavall.hytale.resourcegame.controlserver.web;

import com.tavall.hytale.resourcegame.middleware.control.CommandIssuedFrom;
import com.tavall.hytale.resourcegame.middleware.control.ControlCommand;
import com.tavall.hytale.resourcegame.middleware.control.ControlCommandResult;
import com.tavall.hytale.resourcegame.middleware.control.ControlCommandRuntime;
import com.tavall.hytale.resourcegame.middleware.control.ControlOperator;

import java.time.Instant;

public final class WebControlCommandSubmissionHandler {
    private final ControlCommandRuntime runtime;
    private final ControlOperator operator;

    public WebControlCommandSubmissionHandler(ControlCommandRuntime runtime, ControlOperator operator) {
        this.runtime = runtime;
        this.operator = operator;
    }

    public ControlCommandResult submitCommandLine(String commandLine, boolean dryRun) {
        String input = dryRun && !commandLine.trim().toLowerCase().startsWith("dry-run")
                ? "dry-run " + commandLine
                : commandLine;
        ControlCommand command = runtime.parsingHandler().parseConsoleCommand(input, operator, CommandIssuedFrom.WEB_PANEL, Instant.now());
        return runtime.dispatchHandler().dispatchCommand(command);
    }
}
