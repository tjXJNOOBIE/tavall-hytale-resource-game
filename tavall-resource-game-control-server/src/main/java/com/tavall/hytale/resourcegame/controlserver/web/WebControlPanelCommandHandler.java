package com.tavall.hytale.resourcegame.controlserver.web;

import com.tavall.hytale.resourcegame.middleware.control.ControlCommandResult;

public final class WebControlPanelCommandHandler {
    private final WebControlCommandSubmissionHandler commandSubmissionHandler;

    public WebControlPanelCommandHandler(WebControlCommandSubmissionHandler commandSubmissionHandler) {
        this.commandSubmissionHandler = commandSubmissionHandler;
    }

    public ControlCommandResult submitCommandLine(String commandLine, boolean dryRun) {
        return commandSubmissionHandler.submitCommandLine(commandLine, dryRun);
    }
}
