package com.tavall.resourcegame.controlserver.web;

import com.tavall.resourcegame.middleware.control.ControlCommandResult;

public interface IWebControlPanelCommandHandler {
    ControlCommandResult submitCommandLine(String commandLine, boolean dryRun);
}
