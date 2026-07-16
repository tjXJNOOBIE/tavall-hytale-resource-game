package org.tavall.control.web;

import org.tavall.control.ControlServerDomain;
import org.tavall.control.runtime.CommandIssuedFrom;
import org.tavall.control.runtime.ControlCommand;
import org.tavall.control.runtime.ControlCommandResult;

import java.time.Instant;

public final class WebControlCommandSubmissionHandler implements IWebControlCommandSubmissionHandler, ControlServerDomain {
    public ControlCommandResult submitCommandLine(String commandLine, boolean dryRun) {
        String input = dryRun && !commandLine.trim().toLowerCase().startsWith("dry-run")
                ? "dry-run " + commandLine
                : commandLine;
        ControlCommand command = getControlCommandRuntime().parsingHandler().parseConsoleCommand(input, getWebControlOperator(), CommandIssuedFrom.WEB_PANEL, Instant.now());
        return getControlCommandRuntime().dispatchHandler().dispatchCommand(command);
    }
}
