package com.tavall.resourcegame.controlserver.web;

import com.tavall.resourcegame.controlserver.IControlServerDomain;
import com.tavall.resourcegame.middleware.control.CommandIssuedFrom;
import com.tavall.resourcegame.middleware.control.ControlCommand;
import com.tavall.resourcegame.middleware.control.ControlCommandResult;

import java.time.Instant;

public final class WebControlCommandSubmissionHandler implements IWebControlCommandSubmissionHandler, IControlServerDomain {
    public ControlCommandResult submitCommandLine(String commandLine, boolean dryRun) {
        String input = dryRun && !commandLine.trim().toLowerCase().startsWith("dry-run")
                ? "dry-run " + commandLine
                : commandLine;
        ControlCommand command = getControlCommandRuntime().parsingHandler().parseConsoleCommand(input, getWebControlOperator(), CommandIssuedFrom.WEB_PANEL, Instant.now());
        return getControlCommandRuntime().dispatchHandler().dispatchCommand(command);
    }
}
