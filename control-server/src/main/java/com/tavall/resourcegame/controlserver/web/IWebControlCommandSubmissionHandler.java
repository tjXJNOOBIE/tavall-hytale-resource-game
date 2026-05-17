package com.tavall.resourcegame.controlserver.web;

import com.tavall.resourcegame.middleware.control.ControlCommandResult;

public interface IWebControlCommandSubmissionHandler {
    ControlCommandResult submitCommandLine(String commandLine, boolean dryRun);
}
