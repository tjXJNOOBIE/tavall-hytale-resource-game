package com.tavall.resourcegame.controlserver.web;

import com.tavall.resourcegame.controlserver.IControlServerDomain;
import com.tavall.resourcegame.middleware.control.ControlCommandResult;

public final class WebControlPanelCommandHandler implements IWebControlPanelCommandHandler, IControlServerDomain {
    public ControlCommandResult submitCommandLine(String commandLine, boolean dryRun) {
        return getWebControlCommandSubmissionHandler().submitCommandLine(commandLine, dryRun);
    }
}
